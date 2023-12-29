package me.byteful.plugin.leveltools;

import me.byteful.plugin.leveltools.api.AnvilCombineMode;
import me.byteful.plugin.leveltools.listeners.AnvilListener;
import me.byteful.plugin.leveltools.listeners.BlockEventListener;
import me.byteful.plugin.leveltools.listeners.EntityEventListener;
import me.byteful.plugin.leveltools.util.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;
import redempt.crunch.functional.EvaluationEnvironment;
import redempt.redlib.blockdata.BlockDataManager;
import redempt.redlib.misc.Task;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class LevelToolsPlugin extends JavaPlugin {
    private static LevelToolsPlugin instance;

    private BlockDataManager blockDataManager;
    private BukkitCommandHandler commandManager;
    private AnvilCombineMode anvilCombineMode;
    private UpdateChecker updateChecker;
    private CompiledExpression levelXpFormula;

    public static LevelToolsPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        updateChecker = new UpdateChecker(this);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        final Path blocksFile = getDataFolder().toPath().resolve("player_placed_blocks.db");

        if (!Files.exists(blocksFile)) {
            try {
                blocksFile.toFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        blockDataManager = BlockDataManager.createSQLite(this, blocksFile, true, true);
        blockDataManager.migrate();
        getLogger().info("Loaded BlockDataManager...");

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        setAnvilCombineMode();
        setLevelXpFormula();
        getLogger().info("Loaded configuration...");

        if (getConfig().getBoolean("update.start")) {
            updateChecker.check();
        }

        if (getConfig().getBoolean("update.periodically")) {
            final long delay = 20L * TimeUnit.DAYS.toSeconds(1);
            Task.syncRepeating(() -> updateChecker.check(), delay, delay);
        }

        registerListeners();
        getLogger().info("Registered listeners...");

        commandManager = BukkitCommandHandler.create(this);
        commandManager.setHelpWriter((command, actor) -> String.format("&7- &b/%s %s&7: &e%s", command.getPath().toRealString(), command.getUsage(), command.getDescription()));
        commandManager.register(new LevelToolsCommand());
        commandManager.registerBrigadier();
        getLogger().info("Registered commands...");

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LevelToolsPlaceholders().register();
        }

        getLogger().info("Successfully started " + getDescription().getFullName() + "!");
    }

    @Override
    public void onDisable() {
        if (blockDataManager != null) {
            blockDataManager.saveAndClose();
            blockDataManager = null;
        }

        instance = null;

        getLogger().info("Successfully stopped " + getDescription().getFullName() + ".");
    }

    private void registerListeners() {
        final PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BlockEventListener(), this);
        pm.registerEvents(new EntityEventListener(), this);
        pm.registerEvents(new AnvilListener(), this);
    }

    public void setAnvilCombineMode() {
        anvilCombineMode = AnvilCombineMode.fromName(Objects.requireNonNull(getConfig().getString("anvil_combine")));
    }

    public void setLevelXpFormula() {
        final EvaluationEnvironment env = new EvaluationEnvironment();
        env.setVariableNames("{current_level}");
        levelXpFormula = Crunch.compileExpression(getConfig().getString("level_xp_formula"), env);
    }

    public BlockDataManager getBlockDataManager() {
        return blockDataManager;
    }

    public AnvilCombineMode getAnvilCombineMode() {
        return anvilCombineMode;
    }

    public CompiledExpression getLevelXpFormula() {
        return levelXpFormula;
    }

    public BukkitCommandHandler getCommandManager() {
        return commandManager;
    }
}
