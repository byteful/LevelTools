package me.byteful.plugin.leveltools.api.item;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.LevelToolsUtil;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface LevelToolsItem {
  @NotNull
  ItemStack getItemStack();

  int getLevel();

  void setLevel(int level);

  double getXp();

  void setXp(double xp);

  default double getMaxXp() {
    final double xpStart = LevelToolsPlugin.getInstance().getConfig().getDouble("level_xp_start");
    double increaseAmount =
        LevelToolsPlugin.getInstance().getConfig().getDouble("level_xp_increase.amount");
    final String mode =
        LevelToolsPlugin.getInstance().getConfig().getString("level_xp_increase.mode");

    if (xpStart < 1.0) {
      throw new RuntimeException(
          "Failed to find valid value for 'level_xp_start'. Please make sure it is equal to or over 1.0. Check your configuration!");
    }

    if (increaseAmount < 1.0) {
      throw new RuntimeException(
          "Failed to find valid value for 'level_xp_increase -> amount'. Please make sure it is equal to or over 1.0. Check your configuration!");
    }

    if (mode == null) {
      throw new RuntimeException(
          "Failed to find valid value for 'level_xp_increase -> mode'. Please check your configuration!");
    }

    double nextXp = xpStart;

    if (getLevel() > 0) {
      if (mode.equalsIgnoreCase("ADD")) {
        nextXp = xpStart + (getLevel() * increaseAmount);
      } else if (mode.equalsIgnoreCase("MULTIPLY")) {
        nextXp = xpStart * (Math.pow(increaseAmount, getLevel() * 1.0));
      } else {
        throw new RuntimeException(
            "Mode for 'level_xp_increase' is not 'ADD' or 'MULTIPLY'. Please check your configuration!");
      }
    }

    final double rounded = LevelToolsUtil.round(nextXp, 1);

    if (rounded <= 0.0) {
      throw new RuntimeException(
          "Failed to round "
              + nextXp
              + " to the first place. Please modify your values to get a better result! Check your configuration!");
    }

    return rounded;
  }

  void enchant(Enchantment enchantment, int level);

  void modifyAttribute(String attribute, double modifier);
}
