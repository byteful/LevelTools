package me.byteful.plugin.leveltools.listeners;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.ActionBar;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.LevelToolsUtil;
import me.byteful.plugin.leveltools.Text;
import me.byteful.plugin.leveltools.api.event.LevelToolsLevelIncreaseEvent;
import me.byteful.plugin.leveltools.api.event.LevelToolsXPIncreaseEvent;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Objects;

public abstract class LevelToolsXPListener implements Listener {
    protected void handle(LevelToolsItem tool, Player player, double modifier) {
        double newXp = LevelToolsUtil.round(tool.getXp() + modifier, 1);

        final LevelToolsXPIncreaseEvent xpEvent =
                new LevelToolsXPIncreaseEvent(tool, player, newXp, false);

        Bukkit.getPluginManager().callEvent(xpEvent);

        if (xpEvent.isCancelled()) {
            return;
        }

        tool.setXp(xpEvent.getNewXp());

        if (Config.actionBarEnabled) {
            final String text =
                    Text.colorize(
                            Objects.requireNonNull(LevelToolsPlugin.getInstance()
                                                           .getConfig()
                                                           .getString(Config.actionBarDisplay))
                                    .replace(
                                            "{progress_bar}",
                                            LevelToolsUtil.createDefaultProgressBar(tool.getXp(), tool.getMaxXp()))
                                    .replace("{xp}", String.valueOf(tool.getXp()))
                                    .replace("{max_xp}", String.valueOf(tool.getMaxXp())));

            //      player
            //          .spigot()
            //          .sendMessage(                                  DOESN'T WORK ON 1.8
            //              ChatMessageType.ACTION_BAR,
            //              TextComponent.fromLegacyText(text));
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

            final XSound parsed = XSound.matchXSound(Config.levelUpSound).orElse(null);

            if (parsed != null && parsed.isSupported() && parsed.parseSound() != null) {
                player.playSound(
                        player.getLocation(),
                        parsed.parseSound(),
                        Config.pitch,
                        Config.volume);
            }
        }
        LevelToolsUtil.setHand(player, tool.getItemStack());
    }
}
