package me.byteful.plugin.leveltools.util;

import static me.byteful.plugin.leveltools.util.Text.*;
import static net.kyori.adventure.text.Component.translatable;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.messages.ActionBar;
import com.google.common.base.Strings;
import de.tr7zw.changeme.nbtapi.NBTItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.RewardType;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.item.impl.NBTLevelToolsItem;
import me.byteful.plugin.leveltools.api.item.impl.PDCLevelToolsItem;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import me.byteful.plugin.leveltools.api.scheduler.impl.bukkit.BukkitScheduler;
import me.byteful.plugin.leveltools.api.scheduler.impl.folia.FoliaScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public final class LevelToolsUtil {
  private static final String LORE_PREFIX = "&f&l&o&n&m&k";

  private static final boolean IS_PAPER = hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");
  public static final int MID_VERSION;

  static {
    // Code from https://github.com/boxbeam/RedLib/blob/master/src/redempt/redlib/java
    final Pattern pattern = Pattern.compile("1\\.([0-9]+)");
    final Matcher matcher = pattern.matcher(Bukkit.getBukkitVersion());
    matcher.find();
    MID_VERSION = Integer.parseInt(matcher.group(1));
  }

  private static boolean hasClass(String name) {
    try {
      Class.forName(name);
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

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

    return colorize(
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

    final Double custom = getCustomModifier(combat_xp_modifiers, entityType.name());
    if (custom != null) return custom;

    final ConfigurationSection default_combat_xp_modifier =
        LevelToolsPlugin.getInstance()
            .getConfig()
            .getConfigurationSection("default_combat_xp_modifier");

    return calculateFromRange(default_combat_xp_modifier);
  }

  public static double getBlockModifier(Material material) {
    final ConfigurationSection block_xp_modifiers =
        LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("block_xp_modifiers");

    final Double custom = getCustomModifier(block_xp_modifiers, material.name());
    if (custom != null) return custom;

    final ConfigurationSection default_block_xp_modifier =
        LevelToolsPlugin.getInstance()
            .getConfig()
            .getConfigurationSection("default_block_xp_modifier");

    return calculateFromRange(default_block_xp_modifier);
  }

  private static Double getCustomModifier(ConfigurationSection config, String type) {
    for (String modifier : config.getKeys(false)) {
      if (modifier.equalsIgnoreCase(type)) {
        final ConfigurationSection modifierCs = config.getConfigurationSection(modifier);

        return calculateFromRange(modifierCs);
      }
    }
    return null;
  }

  @NotNull
  private static Double calculateFromRange(ConfigurationSection modifierCs) {
    double min = modifierCs.getDouble("min");
    double max = modifierCs.getDouble("max");
    if (Double.compare(min, max) == 0) {
      return min;
    }
    if (min > max) {
      double hold = min;
      min = max;
      max = hold;
    }
    return round(ThreadLocalRandom.current().nextDouble(min, max), 1);
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
        || (MID_VERSION >= 14 && material == XMaterial.CROSSBOW.parseMaterial());
  }

  public static boolean isSupportedTool(Material material) {
    return isPickaxe(material)
        || isAxe(material)
        || isShovel(material)
        || isSword(material)
        || isProjectileShooter(material);
  }

  public static ItemStack getHand(Player player) {
    return MID_VERSION >= 9
        ? player.getInventory().getItemInMainHand().clone()
        : player.getItemInHand().clone();
  }

  public static void setHand(Player player, ItemStack stack) {
    if (MID_VERSION >= 9) {
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
    if (MID_VERSION >= 14) {
      if (MID_VERSION < 18) {
        final NBTItem nbt = new NBTItem(stack);
        if (nbt.getKeys().stream().anyMatch(s -> s.startsWith("levelTools"))) {
          return new NBTLevelToolsItem(
              stack); // Support tools created with "old" NBT system for 1.14+.
        }
      }

      return new PDCLevelToolsItem(stack);
    } else {
      return new NBTLevelToolsItem(stack);
    }
  }

  public static String getServerVersion() {
    String version = Bukkit.getVersion();
    String[] split = version.split(" ");
    return split[split.length - 1].trim().replace(")", "");
  }

  public static ItemStack buildItemStack(
      ItemStack stack, Map<Enchantment, Integer> enchantments, int level, double xp, double maxXp) {
    final ConfigurationSection cs =
        LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("display");

    final ItemMeta meta = stack.getItemMeta();
    assert meta != null : "ItemMeta is null! Should not happen.";
    final String progressBar = LevelToolsUtil.createDefaultProgressBar(xp, maxXp);
    if (cs.getBoolean("name.enabled")) {
      final String text = colorize(cs.getString("name.text")
              .replace("{level}", String.valueOf(level))
              .replace("{xp}", String.valueOf(xp))
              .replace("{max_xp}", String.valueOf(maxXp))
              .replace("{max_xp_formatted}", formatMoney(maxXp))
              .replace("{xp_formatted}", formatMoney(xp))
              .replace("{progress_bar}", progressBar));

      // We have to do some JSON component magic to make this work right
      if (cs.getString("name.text").contains("{item}") && IS_PAPER) {
        final net.kyori.adventure.text.TextComponent component = LegacyComponentSerializer.legacySection().deserialize(text);
        for (Component child : component.children()) {
          if (child instanceof net.kyori.adventure.text.TextComponent && ((net.kyori.adventure.text.TextComponent) child).content().equals("{item}")) {
            ((net.kyori.adventure.text.TextComponent) child).content("");
            child.append(translatable(stack.getType().translationKey()));
          }
        }
      } else {
        meta.setDisplayName(text);
      }
    }
    if (cs.getBoolean("lore.enabled")) {
      List<String> lines =
          cs.getStringList("lore.lines").stream()
              .map(str -> LORE_PREFIX + str)
              .map(
                  str ->
                      colorize(
                              str.replace("{level}", String.valueOf(level))
                                  .replace("{xp}", String.valueOf(xp))
                                  .replace("{max_xp}", String.valueOf(maxXp))
                                  .replace("{progress_bar}", progressBar))
                          .replace("{max_xp_formatted}", formatMoney(maxXp))
                          .replace("{xp_formatted}", formatMoney(xp)))
              .collect(Collectors.toList());
      smartSetLore(meta, lines);
    }
    for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
      meta.addEnchant(entry.getKey(), entry.getValue(), true);
    }
    if (LevelToolsPlugin.getInstance().getConfig().getBoolean("hide_attributes", true)) {
      meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    }
    stack.setItemMeta(meta);

    return stack;
  }

  private static void smartSetLore(@NotNull ItemMeta meta, @NotNull List<String> toAdd) {
    final List<String> lore = meta.getLore();
    if (!meta.hasLore() || lore == null) {
      meta.setLore(toAdd);

      return;
    }

    final int[] bounds = findPrefixBounds(lore);
    final int start = bounds[0];
    final int end = bounds[1];
    if (start == -1) {
      lore.addAll(toAdd);
      meta.setLore(lore);

      return;
    }
    if (end > lore.size()) {
      meta.setLore(toAdd);

      return;
    }
    final List<String> sub = lore.subList(start, end);
    sub.clear();
    sub.addAll(toAdd);
    meta.setLore(lore);
  }

  private static int[] findPrefixBounds(@NotNull List<String> lore) {
    final int[] arr = new int[]{-1, -1};
    for (int i = 0; i < lore.size(); i++) {
      if (decolorize(lore.get(i)).startsWith(LORE_PREFIX)) {
        if (arr[0] == -1) {
          arr[0] = i;
          arr[1] = i;
        } else {
          arr[1] = i;
        }
      }
    }

    return arr;
  }

  public static void handleReward(LevelToolsItem tool, Player player) {
    final ConfigurationSection rewardCs = getCsFromType(tool.getItemStack().getType());

    for (String key : rewardCs.getKeys(false)) {
      if (!NumberUtils.isNumber(key)) {
        continue;
      }

      final int keyNum = Integer.parseInt(key);
      if (keyNum < 0) continue;
      if (keyNum != tool.getLevel()) continue;
      if (tool.getLastHandledReward() == keyNum) return;

      tool.setLastHandledReward(keyNum);
      setHand(player, tool.getItemStack());
      for (String rewardStr : rewardCs.getStringList(key)) {
        final String[] split = rewardStr.split(" ");

        if (split.length < 2) {
          continue;
        }

        RewardType.fromConfigKey(
                split[0].toLowerCase(Locale.ROOT).trim().replace(" ", "-").replace("_", "-"))
            .ifPresent(
                type -> {
                  type.apply(tool, split, player);
                  if (type.isShouldUpdate()) {
                    setHand(player, tool.getItemStack());
                  }
                });
      }

      return;
    }
  }

  private static ConfigurationSection getCsFromType(Material material) {
    if (LevelToolsUtil.isSword(material)) {
      return LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("sword_rewards");
    } else if (LevelToolsUtil.isProjectileShooter(material)) {
      return LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("bow_rewards");
    } else {
      return LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("tool_rewards");
    }
  }

  public static void sendActionBar(Player player, String msg) {
    if (MID_VERSION > 12) {
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
    } else {
      ActionBar.sendActionBar(player, msg);
    }
  }

  public static Scheduler createScheduler(LevelToolsPlugin plugin) {
    if (isFolia()) {
      return new FoliaScheduler(plugin);
    }

    return new BukkitScheduler(plugin);
  }

  private static boolean isFolia() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
