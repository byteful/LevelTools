package me.byteful.plugin.leveltools;

import com.cryptomorin.xseries.XMaterial;
import com.google.common.base.Strings;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.item.impl.NBTLevelToolsItem;
import me.byteful.plugin.leveltools.api.item.impl.PDCLevelToolsItem;
import me.lucko.helper.reflect.MinecraftVersion;
import me.lucko.helper.text3.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import redempt.redlib.RedLib;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class LevelToolsUtil {
  public static String getProgressBar(
      double percent,
      int totalBars,
      String prefixSymbol,
      String suffixSymbol,
      String barSymbol,
      ChatColor prefixColor,
      ChatColor suffixColor,
      ChatColor completedColor,
      ChatColor placeholderColor) {
    int progressBars = roundDown(totalBars * percent);

    return Text.colorize(
        ""
            + prefixColor
            + prefixSymbol
            + Strings.repeat("" + completedColor + barSymbol, progressBars)
            + Strings.repeat("" + placeholderColor + barSymbol, Math.abs(totalBars - progressBars))
            + suffixColor
            + suffixSymbol);
  }

  public static double getCombatModifier(EntityType entityType) {
    final ConfigurationSection combat_xp_modifiers =
        LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("combat_xp_modifiers");

    for (String modifier : combat_xp_modifiers.getKeys(false)) {
      if (modifier.equalsIgnoreCase(entityType.name())) {
        final ConfigurationSection modifierCs =
            combat_xp_modifiers.getConfigurationSection(modifier);

        return round(
            ThreadLocalRandom.current()
                .nextDouble(modifierCs.getDouble("min"), modifierCs.getDouble("max")),
            1);
      }
    }

    final ConfigurationSection default_combat_xp_modifier =
        LevelToolsPlugin.getInstance()
            .getConfig()
            .getConfigurationSection("default_combat_xp_modifier");

    return round(
        ThreadLocalRandom.current()
            .nextDouble(
                default_combat_xp_modifier.getDouble("min"),
                default_combat_xp_modifier.getDouble("max")),
        1);
  }

  public static double getBlockModifier(Material material) {
    final ConfigurationSection block_xp_modifiers =
        LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("block_xp_modifiers");

    for (String modifier : block_xp_modifiers.getKeys(false)) {
      if (modifier.equalsIgnoreCase(material.name())) {
        final ConfigurationSection modifierCs =
            block_xp_modifiers.getConfigurationSection(modifier);

        return round(
            ThreadLocalRandom.current()
                .nextDouble(modifierCs.getDouble("min"), modifierCs.getDouble("max")),
            1);
      }
    }

    final ConfigurationSection default_block_xp_modifier =
        LevelToolsPlugin.getInstance()
            .getConfig()
            .getConfigurationSection("default_block_xp_modifier");

    return round(
        ThreadLocalRandom.current()
            .nextDouble(
                default_block_xp_modifier.getDouble("min"),
                default_block_xp_modifier.getDouble("max")),
        1);
  }

  public static boolean isPickaxe(Material material) {
    return material.name().endsWith("_PICKAXE");
  }

  public static boolean isAxe(Material material) {
    return material.name().endsWith("_AXE");
  }

  public static boolean isShovel(Material material) {
    return material.name().endsWith("_SHOVEL");
  }

  public static boolean isSword(Material material) {
    return material.name().endsWith("_SWORD");
  }

  public static boolean isProjectileShooter(Material material) {
    return material == XMaterial.BOW.parseMaterial()
        || (RedLib.MID_VERSION >= 14 && material == XMaterial.CROSSBOW.parseMaterial());
  }

  public static ItemStack getHand(Player player) {
    return MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersion.parse("1.9"))
        ? player.getInventory().getItemInMainHand().clone()
        : player.getItemInHand().clone();
  }

  public static void setHand(Player player, ItemStack stack) {
    if (MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersion.parse("1.9"))) {
      player.getInventory().setItemInMainHand(stack);
    } else {
      player.setItemInHand(stack);
    }
  }

  public static String createDefaultProgressBar(double xp, double maxXp) {
    ConfigurationSection cs =
        LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("progress_bar");

    return LevelToolsUtil.getProgressBar(
        (xp / maxXp),
        cs.getInt("total_bars"),
        cs.getString("prefix_symbol"),
        cs.getString("suffix_symbol"),
        cs.getString("bar_symbol"),
        ChatColor.getByChar(cs.getString("prefix_color")),
        ChatColor.getByChar(cs.getString("suffix_color")),
        ChatColor.getByChar(cs.getString("completed_color")),
        ChatColor.getByChar(cs.getString("placeholder_color")));
  }

  public static double round(double value, int places) {
    if (places < 0) throw new IllegalArgumentException();

    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);

    return bd.doubleValue();
  }

  public static int roundDown(double value) {
    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(1, RoundingMode.DOWN);

    return bd.intValue();
  }

  public static LevelToolsItem createLevelToolsItem(ItemStack stack) {
    if (RedLib.MID_VERSION >= 14) {
      if (RedLib.MID_VERSION < 18) {
        final NBTItem nbt = new NBTItem(stack);
        if (nbt.getKeys().stream().anyMatch(s -> s.startsWith("levelTools"))) {
          return new NBTLevelToolsItem(stack); // Support tools created with "old" NBT system for 1.14+.
        }
      }

      return new PDCLevelToolsItem(stack);
    } else {
      return new NBTLevelToolsItem(stack);
    }
  }

  public static ItemStack buildItemStack(ItemStack stack, Map<Enchantment, Integer> enchantments, int level, double xp, double maxXp) {
    final ConfigurationSection cs =
        LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("display");
    List<String> lore = cs.getStringList("default");

    for (String key : cs.getKeys(false)) {
      if (key.equalsIgnoreCase(stack.getType().name())) {
        lore = cs.getStringList(key);
      }
    }

    lore =
        lore.stream()
            .map(
                str ->
                    Text.colorize(
                        str.replace("{level}", String.valueOf(level))
                            .replace("{xp}", String.valueOf(xp))
                            .replace("{max_xp}", String.valueOf(maxXp))
                            .replace(
                                "{progress_bar}",
                                LevelToolsUtil.createDefaultProgressBar(xp, maxXp))))
            .collect(Collectors.toList());

    final ItemMeta meta = stack.getItemMeta();
    meta.setLore(lore);
    stack.setItemMeta(meta);

    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
      stack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
    }

    return stack;
  }
}
