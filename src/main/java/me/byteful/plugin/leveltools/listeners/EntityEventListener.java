package me.byteful.plugin.leveltools.listeners;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class EntityEventListener extends XPListener {
  private Set<EntityType> cachedEntityList;
  private boolean isWhitelist;

  public EntityEventListener() {
    reloadCache();
  }

  public void reloadCache() {
    final LevelToolsPlugin plugin = LevelToolsPlugin.getInstance();
    final String type = plugin.getConfig().getString("entity_list_type", "blacklist");
    this.isWhitelist = "whitelist".equalsIgnoreCase(type);
    this.cachedEntityList = plugin.getConfig().getStringList("entity_list").stream()
        .map(str -> {
          try {
            return EntityType.valueOf(str);
          } catch (Exception ignored) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEntityKillEntity(EntityDeathEvent e) {
    Player killer = e.getEntity().getKiller();

    if (killer == null || !killer.hasPermission("leveltools.enabled")) {
      return;
    }

    final ItemStack hand = LevelToolsUtil.getHand(killer);

    final boolean inList = cachedEntityList.contains(e.getEntityType());
    if (isWhitelist && !inList) {
      return;
    }
    if (!isWhitelist && inList) {
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
