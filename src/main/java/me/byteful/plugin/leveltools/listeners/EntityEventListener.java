package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.stream.Stream;

public class EntityEventListener extends XPListener {
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityKillEntity(EntityDeathEvent e) {
    Player killer = e.getEntity().getKiller();

    if (killer == null || !killer.hasPermission("leveltools.enabled")) {
      return;
    }

    final ItemStack hand = LevelToolsUtil.getHand(killer);

    final String ltype = LevelToolsPlugin.getInstance().getConfig().getString("entity_list_type", "blacklist");
    final Stream<EntityType> stream = LevelToolsPlugin.getInstance().getConfig().getStringList("entity_list").stream()
      .map(str -> {
        try {
          return EntityType.valueOf(str);
        } catch (Exception ignored) {
          return null;
        }
      }).filter(Objects::nonNull);

    if (ltype.equalsIgnoreCase("whitelist") && stream.noneMatch(type -> e.getEntityType() == type)) {
      return;
    }

    if (ltype.equalsIgnoreCase("blacklist") && stream.anyMatch(type -> e.getEntityType() == type)) {
      return;
    }

    if (!LevelToolsUtil.isSword(hand.getType())
      && !LevelToolsUtil.isProjectileShooter(hand.getType())) {
      return;
    }

    handle(
      LevelToolsUtil.createLevelToolsItem(hand),
      killer,
      LevelToolsUtil.getCombatModifier(e.getEntityType()));
  }
}
