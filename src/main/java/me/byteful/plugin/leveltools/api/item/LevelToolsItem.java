package me.byteful.plugin.leveltools.api.item;

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

  double getMaxXp();

  void enchant(Enchantment enchantment, int level);
}
