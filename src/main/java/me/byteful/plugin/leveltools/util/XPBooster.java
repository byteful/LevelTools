package me.byteful.plugin.leveltools.util;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public final class XPBooster {
    public static double apply(Player player, double xp) {
        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (!permission.getValue() || !permission.getPermission().startsWith("leveltools.booster."))
                continue;

            try {
                xp *= Double.parseDouble(permission.getPermission().substring(19));
            } catch (Exception e) {
                LevelToolsPlugin.getInstance()
                        .getLogger()
                        .warning(
                                "Failed to parse LevelTools XPBooster permission: " + permission.getPermission());
            }
        }

        return xp;
    }
}
