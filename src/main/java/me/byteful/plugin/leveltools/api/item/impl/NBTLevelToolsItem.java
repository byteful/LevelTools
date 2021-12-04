package me.byteful.plugin.leveltools.api.item.impl;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.byteful.plugin.leveltools.LevelToolsUtil;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NBTLevelToolsItem implements LevelToolsItem {
  @NotNull
  private static final String LEVEL_KEY = "levelToolsLevel", XP_KEY = "levelToolsXp";

  @NotNull
  private NBTItem nbt;
  @NotNull
  private Map<Enchantment, Integer> enchantments;

  public NBTLevelToolsItem(@NotNull ItemStack stack) {
    this.nbt = new NBTItem(stack);
    this.enchantments = new HashMap<>();
  }

  @NotNull
  @Override
  public ItemStack getItemStack() {
    return LevelToolsUtil.buildItemStack(nbt.getItem().clone(), enchantments, getLevel(), getXp(), getMaxXp());
  }

  @Override
  public int getLevel() {
    if(!nbt.hasKey(LEVEL_KEY)) {
      setLevel(0);
    }

    return nbt.getInteger(LEVEL_KEY);
  }

  @Override
  public void setLevel(int level) {
    if (level < 0) {
      setLevel0(0);

      return;
    }

    setLevel0(level);
  }

  @Override
  public double getXp() {
    if(!nbt.hasKey(XP_KEY)) {
      setXp(0.0D);
    }

    return nbt.getDouble(XP_KEY);
  }

  @Override
  public void setXp(double xp) {
    if (xp < 0.0D) {
      setXp0(0.0D);

      return;
    }

    setXp0(xp);
  }

  private void setLevel0(int level) {
    nbt.setInteger(LEVEL_KEY, level);
  }

  private void setXp0(double xp) {
    nbt.setDouble(XP_KEY, xp);
  }

  @Override
  public void enchant(Enchantment enchantment, int level) {
    enchantments.put(enchantment, level);
  }

  @NotNull
  public NBTItem getNBT() {
    return nbt;
  }

  public void setNBT(@NotNull NBTItem nbt) {
    this.nbt = nbt;
  }

  @NotNull
  public Map<Enchantment, Integer> getEnchantments() {
    return enchantments;
  }

  public void setEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
    this.enchantments = enchantments;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NBTLevelToolsItem that = (NBTLevelToolsItem) o;
    return nbt.equals(that.nbt) && enchantments.equals(that.enchantments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nbt, enchantments);
  }

  @Override
  public String toString() {
    return "NBTLevelToolsItem{" +
        "nbt=" + nbt +
        ", enchantments=" + enchantments +
        '}';
  }
}
