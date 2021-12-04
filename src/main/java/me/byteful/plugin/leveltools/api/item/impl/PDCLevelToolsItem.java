package me.byteful.plugin.leveltools.api.item.impl;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.LevelToolsUtil;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PDCLevelToolsItem implements LevelToolsItem {
  @NotNull
  private static final NamespacedKey LEVEL_KEY = new NamespacedKey(LevelToolsPlugin.getInstance(), "levelToolsLevel"), XP_KEY = new NamespacedKey(LevelToolsPlugin.getInstance(), "levelToolsXp");

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
    final PersistentDataContainer pdc = getItemPDC();

    if(pdc == null) {
      throw new RuntimeException("Failed to find PDC for ItemStack!");
    }

    if(!pdc.has(LEVEL_KEY, PersistentDataType.INTEGER)) {
      setLevel(0);
    }

    return pdc.get(LEVEL_KEY, PersistentDataType.INTEGER);
  }

  @Override
  public void setLevel(int level) {
    final PersistentDataContainer pdc = getItemPDC();

    if(pdc != null) {
      pdc.set(LEVEL_KEY, PersistentDataType.INTEGER, Math.max(level, 0));
    }
  }

  @Override
  public double getXp() {
    final PersistentDataContainer pdc = getItemPDC();

    if(pdc == null) {
      throw new RuntimeException("Failed to find PDC for ItemStack!");
    }

    if(!pdc.has(XP_KEY, PersistentDataType.DOUBLE)) {
      setXp(0.0D);
    }

    return pdc.get(XP_KEY, PersistentDataType.DOUBLE);
  }

  @Override
  public void setXp(double xp) {
    final PersistentDataContainer pdc = getItemPDC();

    if(pdc != null) {
      pdc.set(LEVEL_KEY, PersistentDataType.DOUBLE, Math.max(xp, 0.0));
    }
  }

  @Override
  public void enchant(Enchantment enchantment, int level) {
    enchantments.put(enchantment, level);
  }

  private PersistentDataContainer getItemPDC() {
    final ItemMeta meta = stack.getItemMeta();

    if(meta == null) {
      return null;
    }

    return (PersistentDataContainer) meta;
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
