package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.LevelToolsUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EntityEventListener extends LevelToolsListener {
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onEntityKillEntity(EntityDeathEvent e) {
    Player killer = e.getEntity().getKiller();

    if (killer == null || !killer.hasPermission("leveltools.enabled")) {
      return;
    }

    final ItemStack hand = LevelToolsUtil.getHand(killer);

    if (LevelToolsPlugin.getInstance().getConfig().getStringList("entityBlacklist").stream()
        .map(EntityType::valueOf)
        .anyMatch(type -> e.getEntityType() == type)) {
      return;
    }

    if (LevelToolsUtil.isSword(hand.getType())
        || LevelToolsUtil.isProjectileShooter(hand.getType())) {
      handle(
          LevelToolsUtil.createLevelToolsItem(hand),
          killer,
          LevelToolsUtil.getCombatModifier(e.getEntityType()));
    }
  }
}
