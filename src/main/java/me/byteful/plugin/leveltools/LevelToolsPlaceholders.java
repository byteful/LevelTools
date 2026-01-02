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

import static me.byteful.plugin.leveltools.util.LevelToolsUtil.MID_VERSION;

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

        final ItemStack hand =
                MID_VERSION <= 8
                        ? player.getItemInHand()
                        : player.getInventory().getItemInMainHand();

        if (!LevelToolsUtil.isSupportedTool(hand.getType())) {
            return "N/A";
        }

        final LevelToolsItem item = LevelToolsUtil.createLevelToolsItem(hand);

        switch (params.toLowerCase(Locale.ROOT).replace(" ", "_")) {
            case "level": {
                return "" + item.getLevel();
            }

            case "xp": {
                return "" + item.getXp();
            }

            case "max_xp": {
                return "" + item.getMaxXp();
            }

            case "progress_bar": {
                DisplayProfile displayProfile = getDisplayProfile(hand);
                return LevelToolsUtil.createProgressBar(item.getXp(), item.getMaxXp(), displayProfile);
            }

            case "item_profile": {
                ItemProfile profile = LevelToolsUtil.getItemProfile(hand.getType());
                return profile != null ? profile.getId() : "N/A";
            }

            case "max_level": {
                ItemProfile profile = LevelToolsUtil.getItemProfile(hand.getType());
                return profile != null ? String.valueOf(profile.getMaxLevel()) : "N/A";
            }

            default: {
                return "N/A";
            }
        }
    }

    @Nullable
    private DisplayProfile getDisplayProfile(ItemStack item) {
        ItemProfile itemProfile = LevelToolsUtil.getItemProfile(item.getType());
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
