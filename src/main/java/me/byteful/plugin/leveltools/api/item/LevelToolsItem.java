package me.byteful.plugin.leveltools.api.item;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import redempt.crunch.CompiledExpression;

public interface LevelToolsItem {
    @NotNull
    ItemStack getItemStack();

    int getLevel();

    void setLevel(int level);

    double getXp();

    void setXp(double xp);

    int getLastHandledReward();

    void setLastHandledReward(int rewardKey);

    default double getMaxXp() {
        final CompiledExpression formula = LevelToolsPlugin.getInstance().getLevelXpFormula();
        final double nextXpRequirement = LevelToolsUtil.round(formula.evaluate(getLevel()), 1);

        if (nextXpRequirement <= 0.0) {
            throw new RuntimeException(
                    "The next XP requirement formula returned a value too small! Please optimize your formula.");
        }

        return nextXpRequirement;
    }

    void enchant(Enchantment enchantment, int level);

    void modifyAttribute(String attribute, double modifier);
}
