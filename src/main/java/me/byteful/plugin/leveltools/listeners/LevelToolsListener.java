package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.LevelToolsUtil;
import me.byteful.plugin.leveltools.api.event.LevelToolsLevelIncreaseEvent;
import me.byteful.plugin.leveltools.api.event.LevelToolsXPIncreaseEvent;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.lucko.helper.Events;
import me.lucko.helper.text3.Text;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class LevelToolsListener implements Listener {
  protected void handle(LevelToolsItem tool, Player player, double modifier) {
    double newXp = LevelToolsUtil.round(tool.getXp() + modifier, 1);

    final LevelToolsXPIncreaseEvent xpEvent =
        new LevelToolsXPIncreaseEvent(tool, player, newXp, false);

    Events.call(xpEvent);

    if (xpEvent.isCancelled()) {
      return;
    }

    tool.setXp(xpEvent.getNewXp());

    if (LevelToolsPlugin.getInstance().getConfig().getBoolean("actionBar.enabled")) {
      player
          .spigot()
          .sendMessage(
              ChatMessageType.ACTION_BAR,
              TextComponent.fromLegacyText(
                  Text.colorize(
                      LevelToolsPlugin.getInstance()
                          .getConfig()
                          .getString("actionBar.display")
                          .replace(
                              "{progress_bar}",
                              LevelToolsUtil.createDefaultProgressBar(
                                  tool.getXp(), tool.getMaxXp()))
                          .replace("{xp}", String.valueOf(tool.getXp()))
                          .replace("{max_xp}", String.valueOf(tool.getMaxXp())))));
    }

    if (tool.getXp() >= tool.getMaxXp()) {
      int newLevel = tool.getLevel() + 1;

      final LevelToolsLevelIncreaseEvent levelEvent =
          new LevelToolsLevelIncreaseEvent(tool, player, newLevel, false);

      if (levelEvent.isCancelled()) {
        return;
      }

      tool.setXp(LevelToolsUtil.round(Math.abs(tool.getXp() - tool.getMaxXp()), 1));
      tool.setLevel(levelEvent.getNewLevel());

      handleReward(tool, player);

      final ConfigurationSection soundCs =
          LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("level_up_sound");

      player.playSound(
          player.getLocation(),
          Sound.valueOf(soundCs.getString("sound")),
          (float) soundCs.getDouble("pitch"),
          (float) soundCs.getDouble("volume"));
    }

    LevelToolsUtil.setHand(player, tool.getItemStack());
  }

  private void handleReward(LevelToolsItem tool, Player player) {
    final ConfigurationSection rewardCs = getCsFromType(tool.getItemStack().getType());

    for (String key : rewardCs.getKeys(false)) {
      if (NumberUtils.isNumber(key) && tool.getLevel() == Integer.parseInt(key)) {
        final List<String> rewards = rewardCs.getStringList(key);

        for (String rewardStr : rewards) {
          final String[] split = rewardStr.split(" ");

          if (split.length >= 2) {
            final String handler = split[0];

            switch (handler.toLowerCase(Locale.ROOT).trim()) {
              case "command":
                {
                  Bukkit.dispatchCommand(
                      Bukkit.getConsoleSender(),
                      String.join(" ", Arrays.copyOfRange(split, 1, split.length)).replace("{player}", player.getName()));

                  break;
                }

              case "player-command":
                {
                  Bukkit.dispatchCommand(
                      player, String.join(" ", Arrays.copyOfRange(split, 1, split.length)).replace("{player}", player.getName()));

                  break;
                }

              case "enchant":
                {
                  if (split.length >= 3
                      && Enchantment.getByKey(NamespacedKey.minecraft(split[1])) != null
                      && NumberUtils.isNumber(split[2])) {
                    tool.enchant(
                        Enchantment.getByKey(NamespacedKey.minecraft(split[1])),
                        Integer.parseInt(split[2]));
                  }

                  break;
                }

              case "enchant2":
                {
                  if (split.length >= 3 && NumberUtils.isNumber(split[2])) {
                    final Enchantment enchant =
                        Enchantment.getByKey(NamespacedKey.minecraft(split[1]));
                    final int level = Integer.parseInt(split[2]);

                    if (enchant != null && tool.getItemStack().getEnchantmentLevel(enchant) < level) {
                      tool.enchant(enchant, level);
                    }
                  }

                  break;
                }
            }
          }
        }
      }
    }
  }

  private ConfigurationSection getCsFromType(Material material) {
    if(LevelToolsUtil.isSword(material)) {
      return LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("sword_rewards");
    } else if(LevelToolsUtil.isBowOrCrossbow(material)) {
      return LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("bow_rewards");
    } else {
      return LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("tool_rewards");
    }
  }
}
