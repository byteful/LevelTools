package me.byteful.plugin.leveltools.api.item.impl;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.LevelToolsUtil;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PDCLevelToolsItem implements LevelToolsItem {
  @NotNull
  public static final NamespacedKey LEVEL_KEY = new NamespacedKey(LevelToolsPlugin.getInstance(), "levelToolsLevel"), XP_KEY = new NamespacedKey(LevelToolsPlugin.getInstance(), "levelToolsXp");

  @NotNull
  private ItemStack stack;
  @NotNull
  private Map<Enchantment, Integer> enchantments;

  public PDCLevelToolsItem(@NotNull ItemStack stack) {
    this.stack = stack;
    this.enchantments = new HashMap<>();
  }

  @Override
  public @NotNull ItemStack getItemStack() {
    return LevelToolsUtil.buildItemStack(stack, enchantments, getLevel(), getXp(), getMaxXp());
  }

  @Override
  public int getLevel() {
    final PersistentDataContainer pdc = getItemPDC().getPersistentDataContainer();

    Integer value = pdc.get(LEVEL_KEY, PersistentDataType.INTEGER);

    if (value == null) {
      setLevel(0);

      value = 0;
    }

    return value;
  }

  @Override
  public void setLevel(int level) {
    final PersistentDataHolder holder = getItemPDC();
    final PersistentDataContainer pdc = holder.getPersistentDataContainer();

    pdc.set(LEVEL_KEY, PersistentDataType.INTEGER, Math.max(level, 0));
    stack.setItemMeta((ItemMeta) holder);
  }

  @Override
  public double getXp() {
    final PersistentDataContainer pdc = getItemPDC().getPersistentDataContainer();

    Double value = pdc.get(XP_KEY, PersistentDataType.DOUBLE);

    if (value == null) {
      setXp(0.0D);

      value = 0.0D;
    }

    return value;
  }

  @Override
  public void setXp(double xp) {
    final PersistentDataHolder holder = getItemPDC();
    final PersistentDataContainer pdc = holder.getPersistentDataContainer();

    pdc.set(XP_KEY, PersistentDataType.DOUBLE, Math.max(xp, 0.0));
    stack.setItemMeta((ItemMeta) holder);
  }

  @Override
  public void enchant(Enchantment enchantment, int level) {
    enchantments.put(enchantment, level);
  }

  private PersistentDataHolder getItemPDC() {
    return stack.getItemMeta();
  }

  @NotNull
  public ItemStack getStack() {
    return stack;
  }

  public void setStack(@NotNull ItemStack stack) {
    this.stack = stack;
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
    PDCLevelToolsItem that = (PDCLevelToolsItem) o;
    return stack.equals(that.stack) && enchantments.equals(that.enchantments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(stack, enchantments);
  }

  @Override
  public String toString() {
    return "PDCLevelToolsItem{" +
        "stack=" + stack +
        ", enchantments=" + enchantments +
        '}';
  }
}
