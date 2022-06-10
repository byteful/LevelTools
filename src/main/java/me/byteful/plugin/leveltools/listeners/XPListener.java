package me.byteful.plugin.leveltools.listeners;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.ActionBar;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.event.LevelToolsLevelIncreaseEvent;
import me.byteful.plugin.leveltools.api.event.LevelToolsXPIncreaseEvent;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import me.byteful.plugin.leveltools.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class XPListener implements Listener {
  protected void handle(LevelToolsItem tool, Player player, double modifier) {
    double newXp = LevelToolsUtil.round(tool.getXp() + modifier, 1);

    final LevelToolsXPIncreaseEvent xpEvent =
      new LevelToolsXPIncreaseEvent(tool, player, newXp, false);

    Bukkit.getPluginManager().callEvent(xpEvent);

    if (xpEvent.isCancelled()) {
      return;
    }

    tool.setXp(xpEvent.getNewXp());

    if (LevelToolsPlugin.getInstance().getConfig().getBoolean("display.actionBar.enabled")) {
      final String text =
        Text.colorize(
          LevelToolsPlugin.getInstance()
            .getConfig()
            .getString("display.actionBar.text")
            .replace(
              "{progress_bar}",
              LevelToolsUtil.createDefaultProgressBar(tool.getXp(), tool.getMaxXp()))
            .replace("{xp}", String.valueOf(tool.getXp()))
            .replace("{max_xp}", String.valueOf(tool.getMaxXp()))
            .replace("{level}", String.valueOf(tool.getLevel())));
      ActionBar.sendActionBar(player, text);
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

      LevelToolsUtil.handleReward(tool, player);

      final ConfigurationSection soundCs =
        LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("level_up_sound");

      final String sound = soundCs.getString("sound", null);

      if (sound == null) {
        return;
      }

      final XSound parsed = XSound.matchXSound(sound).orElse(null);

      if (parsed != null && parsed.isSupported()) {
        player.playSound(
          player.getLocation(),
          parsed.parseSound(),
          (float) soundCs.getDouble("pitch"),
          (float) soundCs.getDouble("volume"));
      }
    }

    LevelToolsUtil.setHand(player, tool.getItemStack());
  }
}
