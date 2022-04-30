package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsUtil;
import me.byteful.plugin.leveltools.config.Config;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EntityEventListener extends LevelToolsXPListener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityKillEntity(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        Entity killed = e.getEntity();

        if (killer == null || !killer.hasPermission("leveltools.enabled")) {
            return;
        }

        final ItemStack hand = LevelToolsUtil.getHand(killer);

        if (Config.entityBlacklist.stream().anyMatch(type -> killed.getType() == type)) return;

        if (LevelToolsUtil.isSword(hand.getType()) || LevelToolsUtil.isProjectileShooter(hand.getType())) {
            handle(LevelToolsUtil.createLevelToolsItem(hand), killer, LevelToolsUtil.getCombatModifier(e.getEntityType()));
        }
    }
}
