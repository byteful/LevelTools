package me.byteful.plugin.leveltools.config;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class XpFormulaRegistry {
    public static final String GLOBAL_ID = "global";
    private static final String DEFAULT_FORMULA = "100 + {current_level} * 100";
    private static final Pattern FORMULA_ID_PATTERN = Pattern.compile("^[a-z0-9_-]+$");
    private static final String FORMULA_PERMISSION_PREFIX = "leveltools.formula.";
    private static final int VALIDATION_MAX_LEVEL = 1000;

    private final LinkedHashMap<String, CompiledExpression> formulas;
    private final List<PermissionFormula> permissionFormulas;
    private final Map<String, CompiledExpression> customFormulaCache = new ConcurrentHashMap<>();
    private final Set<CompiledExpression> clampWarnedFormulas = ConcurrentHashMap.newKeySet();

    private XpFormulaRegistry(@NotNull LinkedHashMap<String, CompiledExpression> formulas) {
        this.formulas = formulas;

        List<PermissionFormula> resolved = new ArrayList<>();
        for (Map.Entry<String, CompiledExpression> entry : formulas.entrySet()) {
            if (!GLOBAL_ID.equals(entry.getKey())) {
                resolved.add(new PermissionFormula(FORMULA_PERMISSION_PREFIX + entry.getKey(), entry.getValue()));
            }
        }
        this.permissionFormulas = resolved;
    }

    @NotNull
    public static XpFormulaRegistry load(@NotNull ConfigurationSection config) {
        ConfigurationSection formulasSection = config.getConfigurationSection("xp_formulas");
        LinkedHashMap<String, CompiledExpression> compiled = new LinkedHashMap<>();

        if (formulasSection != null) {
            for (String id : formulasSection.getKeys(false)) {
                String normalizedId = normalizeId(id);
                if (!id.trim().equals(normalizedId) || !FORMULA_ID_PATTERN.matcher(normalizedId).matches()) {
                    throw new IllegalArgumentException("Invalid XP formula id '" + id + "'. Use only lowercase letters, numbers, dashes, and underscores.");
                }

                if (compiled.containsKey(normalizedId)) {
                    throw new IllegalArgumentException("Duplicate XP formula id after normalization: '" + id + "'.");
                }

                String formula = formulasSection.getString(id);
                if (formula == null || formula.trim().isEmpty()) {
                    throw new IllegalArgumentException("XP formula '" + id + "' is empty.");
                }

                compiled.put(normalizedId, compileAndValidate(formula, "xp_formulas." + id));
            }
        }

        if (!compiled.containsKey(GLOBAL_ID)) {
            String legacyFormula = config.getString("level_xp_formula", DEFAULT_FORMULA);
            compiled.put(GLOBAL_ID, compileAndValidate(legacyFormula, "level_xp_formula"));
        }

        return new XpFormulaRegistry(compiled);
    }

    @NotNull
    public CompiledExpression resolveFormula(@Nullable Player player, @Nullable ItemProfile itemProfile) {
        if (itemProfile != null && itemProfile.hasCustomXpFormula()) {
            return customFormulaCache.computeIfAbsent(
                    itemProfile.getLevelXpFormula(),
                    formula -> compileAndValidate(formula, "item profile '" + itemProfile.getId() + "' level_xp_formula")
            );
        }

        if (player != null) {
            for (PermissionFormula entry : permissionFormulas) {
                if (player.isPermissionSet(entry.permission) && player.hasPermission(entry.permission)) {
                    return entry.formula;
                }
            }
        }

        return getGlobalFormula();
    }

    @NotNull
    public CompiledExpression getGlobalFormula() {
        CompiledExpression formula = formulas.get(GLOBAL_ID);
        if (formula == null) {
            throw new IllegalStateException("Missing required xp_formulas.global formula.");
        }
        return formula;
    }

    public double evaluateMaxXp(@Nullable Player player, @Nullable ItemProfile itemProfile, int currentLevel) {
        return evaluate(resolveFormula(player, itemProfile), currentLevel);
    }

    public double evaluateGlobalMaxXp(int currentLevel) {
        return evaluate(getGlobalFormula(), currentLevel);
    }

    @NotNull
    public Map<String, CompiledExpression> getFormulas() {
        return Collections.unmodifiableMap(formulas);
    }

    @NotNull
    private static String normalizeId(@NotNull String id) {
        return id.trim().toLowerCase();
    }

    @NotNull
    public static CompiledExpression compileAndValidate(@NotNull String formula, @NotNull String source) {
        CompiledExpression expression = Crunch.compileExpression(formula.replace("{current_level}", "$1"));
        if (expression.getVariableCount() > 1) {
            throw new IllegalArgumentException("The XP formula at " + source + " may only use the {current_level} variable.");
        }

        for (int level = 0; level <= VALIDATION_MAX_LEVEL; level++) {
            double value = evaluateRaw(expression, level);
            if (value <= 0.0) {
                throw new IllegalArgumentException("The XP formula at " + source + " returned " + value + " for level " + level + ". It must stay positive for levels 0 to " + VALIDATION_MAX_LEVEL + ".");
            }
        }

        return expression;
    }

    private double evaluate(@NotNull CompiledExpression formula, int currentLevel) {
        double nextXpRequirement = evaluateRaw(formula, currentLevel);
        if (nextXpRequirement <= 0.0) {
            if (clampWarnedFormulas.add(formula)) {
                LevelToolsPlugin.getInstance()
                        .getLogger()
                        .warning("The XP formula '" + formula + "' returned " + nextXpRequirement + " for level " + currentLevel + ". Clamping to 1.0. Please fix your formula.");
            }
            return 1.0;
        }
        return nextXpRequirement;
    }

    // Evaluates the stateless Value tree instead of CompiledExpression.evaluate(double), which
    // mutates a shared field and is not thread-safe. This is only safe while no custom Crunch
    // functions are registered, because FunctionCall nodes carry shared mutable state.
    private static double evaluateRaw(@NotNull CompiledExpression formula, int currentLevel) {
        return LevelToolsUtil.round(formula.getValue().getValue(new double[]{currentLevel}), 1);
    }

    private static final class PermissionFormula {
        private final String permission;
        private final CompiledExpression formula;

        private PermissionFormula(@NotNull String permission, @NotNull CompiledExpression formula) {
            this.permission = permission;
            this.formula = formula;
        }
    }
}
