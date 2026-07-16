package me.byteful.plugin.leveltools.util;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class XPBooster implements Listener {
    private static final String PERMISSION_PREFIX = "leveltools.booster.";
    private static final long CACHE_TTL_MILLIS = 5000L;

    private static final Map<UUID, CachedMultiplier> MULTIPLIER_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> REPORTED_BAD_NODES = ConcurrentHashMap.newKeySet();

    public static double apply(Player player, double xp) {
        return xp * getMultiplier(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        MULTIPLIER_CACHE.remove(event.getPlayer().getUniqueId());
    }

    private static double getMultiplier(Player player) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();

        CachedMultiplier cached = MULTIPLIER_CACHE.get(playerId);
        if (cached != null && now - cached.resolvedAt < CACHE_TTL_MILLIS) {
            return cached.multiplier;
        }

        double multiplier = resolveMultiplier(player);
        MULTIPLIER_CACHE.put(playerId, new CachedMultiplier(multiplier, now));

        return multiplier;
    }

    private static double resolveMultiplier(Player player) {
        double multiplier = 1.0;

        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            String node = permission.getPermission();
            if (!permission.getValue() || !node.startsWith(PERMISSION_PREFIX)) {
                continue;
            }

            try {
                multiplier *= Double.parseDouble(node.substring(PERMISSION_PREFIX.length()));
            } catch (NumberFormatException e) {
                if (REPORTED_BAD_NODES.add(node)) {
                    LevelToolsPlugin.getInstance()
                            .getLogger()
                            .warning("Failed to parse LevelTools XPBooster permission: " + node);
                }
            }
        }

        return multiplier;
    }

    private static final class CachedMultiplier {
        private final double multiplier;
        private final long resolvedAt;

        private CachedMultiplier(double multiplier, long resolvedAt) {
            this.multiplier = multiplier;
            this.resolvedAt = resolvedAt;
        }
    }
}
