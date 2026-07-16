package me.byteful.plugin.leveltools;

import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class LevelToolsPlaceholders extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "leveltools";
    }

    @Override
    public @NotNull String getAuthor() {
        return "byteful";
    }

    @Override
    public @NotNull String getVersion() {
        return LevelToolsPlugin.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        final ItemStack hand = LevelToolsUtil.getHand(player);

        if (!LevelToolsUtil.isSupportedTool(hand.getType())) {
            return "N/A";
        }

        switch (params.toLowerCase(Locale.ROOT).replace(" ", "_")) {
            case "level": {
                return "" + LevelToolsUtil.createLevelToolsItem(hand).getLevel();
            }

            case "xp": {
                return "" + LevelToolsUtil.createLevelToolsItem(hand).getXp();
            }

            case "max_xp": {
                LevelToolsItem item = LevelToolsUtil.createLevelToolsItem(hand);
                ItemProfile itemProfile = LevelToolsUtil.getItemProfile(hand.getType());
                return "" + LevelToolsUtil.getMaxXp(player, itemProfile, item);
            }

            case "progress_bar": {
                LevelToolsItem item = LevelToolsUtil.createLevelToolsItem(hand);
                ItemProfile itemProfile = LevelToolsUtil.getItemProfile(hand.getType());
                double maxXp = LevelToolsUtil.getMaxXp(player, itemProfile, item);
                return LevelToolsUtil.createProgressBar(item.getXp(), maxXp, getDisplayProfile(itemProfile));
            }

            case "progress": {
                LevelToolsItem item = LevelToolsUtil.createLevelToolsItem(hand);
                ItemProfile itemProfile = LevelToolsUtil.getItemProfile(hand.getType());
                double maxXp = LevelToolsUtil.getMaxXp(player, itemProfile, item);
                if (maxXp <= 0.0) {
                    return "0";
                }
                return "" + LevelToolsUtil.round((item.getXp() / maxXp) * 100.0, 1);
            }

            case "item_profile": {
                ItemProfile itemProfile = LevelToolsUtil.getItemProfile(hand.getType());
                return itemProfile != null ? itemProfile.getId() : "N/A";
            }

            case "max_level": {
                ItemProfile itemProfile = LevelToolsUtil.getItemProfile(hand.getType());
                return itemProfile != null ? String.valueOf(itemProfile.getMaxLevel()) : "N/A";
            }

            default: {
                return "N/A";
            }
        }
    }

    @Nullable
    private DisplayProfile getDisplayProfile(@Nullable ItemProfile itemProfile) {
        if (itemProfile == null) {
            return null;
        }
        ProfileManager profileManager = LevelToolsPlugin.getInstance().getProfileManager();
        if (profileManager == null) {
            return null;
        }
        return profileManager.getDisplayProfileFor(itemProfile);
    }
}
