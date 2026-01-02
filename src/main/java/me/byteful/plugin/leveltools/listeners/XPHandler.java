package me.byteful.plugin.leveltools.listeners;

import com.cryptomorin.xseries.XSound;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.event.LevelToolsLevelIncreaseEvent;
import me.byteful.plugin.leveltools.api.event.LevelToolsXPIncreaseEvent;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.reward.RewardEntry;
import me.byteful.plugin.leveltools.profile.reward.RewardProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import me.byteful.plugin.leveltools.util.Text;
import me.byteful.plugin.leveltools.util.XPBooster;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.byteful.plugin.leveltools.util.Text.formatMoney;

public final class XPHandler {
    private final ProfileManager profileManager;

    public XPHandler(@NotNull ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    public void handle(@NotNull TriggerContext context, @NotNull LevelToolsItem tool, double modifier) {
        Player player = context.getPlayer();
        ItemProfile itemProfile = context.getItemProfile();

        List<String> disabledWorlds = LevelToolsPlugin.getInstance().getConfig().getStringList("disabled_worlds");
        if (disabledWorlds.contains(player.getWorld().getName())) {
            return;
        }

        modifier = Math.max(0, XPBooster.apply(player, modifier));
        double newXp = LevelToolsUtil.round(tool.getXp() + modifier, 1);

        LevelToolsXPIncreaseEvent xpEvent = new LevelToolsXPIncreaseEvent(tool, player, newXp, newXp, false);
        Bukkit.getPluginManager().callEvent(xpEvent);

        if (xpEvent.isCancelled()) {
            return;
        }

        tool.setXp(xpEvent.getNewXp());

        if (tool.getXp() >= tool.getMaxXp()) {
            handleLevelUp(tool, player, itemProfile);
        }

        LevelToolsUtil.setHand(player, tool.getItemStack());

        showActionBar(tool, player, itemProfile);

        handleReward(tool, player, itemProfile);
    }

    private void handleLevelUp(@NotNull LevelToolsItem tool, @NotNull Player player, @NotNull ItemProfile itemProfile) {
        int newLevel = tool.getLevel() + 1;
        int maxLevel = itemProfile.getMaxLevel();

        if (newLevel > maxLevel) {
            if (tool.getXp() != tool.getMaxXp()) {
                tool.setXp(tool.getMaxXp());
                LevelToolsUtil.setHand(player, tool.getItemStack());
            }
            return;
        }

        LevelToolsLevelIncreaseEvent levelEvent = new LevelToolsLevelIncreaseEvent(tool, player, newLevel, false);
        Bukkit.getPluginManager().callEvent(levelEvent);

        if (levelEvent.isCancelled()) {
            return;
        }

        tool.setXp(LevelToolsUtil.round(Math.abs(tool.getXp() - tool.getMaxXp()), 1));
        tool.setLevel(levelEvent.getNewLevel());

        if (levelEvent.getNewLevel() == maxLevel) {
            tool.setXp(0);
            tool.setLevel(maxLevel);
        }

        playLevelUpSound(player);
    }

    private void playLevelUpSound(@NotNull Player player) {
        ConfigurationSection soundCs = LevelToolsPlugin.getInstance().getConfig().getConfigurationSection("level_up_sound");

        if (soundCs == null) {
            return;
        }

        String sound = soundCs.getString("sound", null);
        if (sound == null) {
            return;
        }

        XSound parsed = XSound.matchXSound(sound).orElse(null);
        if (parsed != null && parsed.isSupported() && parsed.parseSound() != null) {
            player.playSound(
                    player.getLocation(),
                    parsed.parseSound(),
                    (float) soundCs.getDouble("pitch"),
                    (float) soundCs.getDouble("volume")
            );
        }
    }

    private void showActionBar(@NotNull LevelToolsItem tool, @NotNull Player player, @NotNull ItemProfile itemProfile) {
        DisplayProfile displayProfile = profileManager.getDisplayProfileFor(itemProfile);
        if (displayProfile == null) {
            return;
        }

        DisplayProfile.ActionBarDisplay actionBar = displayProfile.getActionBarDisplay();
        if (!actionBar.isEnabled()) {
            return;
        }

        String progressBar = displayProfile.getProgressBar().buildProgressBar(tool.getXp(), tool.getMaxXp());
        String text = Text.colorize(actionBar.getText()
                .replace("{progress_bar}", progressBar)
                .replace("{xp}", String.valueOf(tool.getXp()))
                .replace("{max_xp}", String.valueOf(tool.getMaxXp()))
                .replace("{level}", String.valueOf(tool.getLevel()))
                .replace("{max_xp_formatted}", formatMoney(tool.getMaxXp()))
                .replace("{xp_formatted}", formatMoney(tool.getXp())));

        LevelToolsUtil.sendActionBar(player, text);
    }

    private void handleReward(@NotNull LevelToolsItem tool, @NotNull Player player, @NotNull ItemProfile itemProfile) {
        RewardProfile rewardProfile = profileManager.getRewardProfileFor(itemProfile);
        if (rewardProfile == null) {
            return;
        }

        int level = tool.getLevel();
        if (!rewardProfile.hasRewardsForLevel(level)) {
            return;
        }

        if (tool.getLastHandledReward() == level) {
            return;
        }

        tool.setLastHandledReward(level);
        LevelToolsUtil.setHand(player, tool.getItemStack());

        List<RewardEntry> rewards = rewardProfile.getRewardsForLevel(level);
        for (RewardEntry entry : rewards) {
            entry.apply(tool, player);
            if (entry.shouldUpdateItem()) {
                LevelToolsUtil.setHand(player, tool.getItemStack());
            }
        }
    }
}
