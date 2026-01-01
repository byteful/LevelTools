package me.byteful.plugin.leveltools.listeners;

import java.util.*;
import java.util.stream.Collectors;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockEventListener extends XPListener {
  private final BlockDataManager blockDataManager;
  private final Scheduler scheduler;

  private Set<Material> cachedBlockList;
  private boolean isWhitelist;

  public BlockEventListener(BlockDataManager blockDataManager, Scheduler scheduler) {
    this.blockDataManager = blockDataManager;
    this.scheduler = scheduler;
    reloadCache();
  }

  public void reloadCache() {
    final LevelToolsPlugin plugin = LevelToolsPlugin.getInstance();
    final String type = plugin.getConfig().getString("block_list_type", "blacklist");
    this.isWhitelist = "whitelist".equalsIgnoreCase(type);
    this.cachedBlockList = plugin.getConfig().getStringList("block_list").stream()
        .map(Material::getMaterial)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private boolean isPPBEnabled() {
    return !LevelToolsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks");
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void on(BlockBreakEvent event) {
    if (!isPPBEnabled()) return;

    final Block block = event.getBlock();
    final BlockPosition pos = BlockPosition.fromBukkit(block);
    scheduler.locationDelayed(() -> blockDataManager.removePlacedBlock(pos), block.getLocation(), 1);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void on(BlockPlaceEvent event) {
    if (!isPPBEnabled()) return;

    blockDataManager.addPlacedBlock(BlockPosition.fromBukkit(event.getBlock()));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent e) {
    final Player player = e.getPlayer();

    if (!player.hasPermission("leveltools.enabled")) {
      return;
    }

    final Block block = e.getBlock();
    final ItemStack hand = LevelToolsUtil.getHand(player);

    if (!LevelToolsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks")
        && blockDataManager.isPlacedBlock(BlockPosition.fromBukkit(block))) {
      return;
    }

    final boolean inList = cachedBlockList.contains(block.getType());
    if (isWhitelist && !inList) {
      return;
    }
    if (!isWhitelist && inList) {
      return;
    }

    if (!LevelToolsUtil.isAxe(hand.getType())
        && !LevelToolsUtil.isPickaxe(hand.getType())
        && !LevelToolsUtil.isShovel(hand.getType())) {
      return;
    }

    handle(
        LevelToolsUtil.createLevelToolsItem(hand),
        player,
        LevelToolsUtil.getBlockModifier(block.getType()));
  }
}
