package me.byteful.plugin.leveltools.listeners;

import com.jeff_media.morepersistentdatatypes.DataType;
import java.util.*;
import java.util.stream.Collectors;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.Vector;

public class BlockEventListener extends XPListener {
  private final NamespacedKey trackedBlocks = new NamespacedKey(LevelToolsPlugin.getInstance(), "tracked_blocks");

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockPlaceTracker(BlockPlaceEvent event) {
    if (!LevelToolsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks")) {
      final PersistentDataContainer pdc = event.getBlock().getChunk().getPersistentDataContainer();
      if (!pdc.has(trackedBlocks)) {
        pdc.set(trackedBlocks, DataType.asList(DataType.VECTOR), new ArrayList<>());
      }

      List<Vector> blocks = pdc.get(trackedBlocks, DataType.asList(DataType.VECTOR));
      if (blocks == null) {
        blocks = new ArrayList<>();
      }

      blocks.add(event.getBlock().getLocation().toVector());
      pdc.set(trackedBlocks, DataType.asList(DataType.VECTOR), blocks);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent e) {
    final Player player = e.getPlayer();

    if (!player.hasPermission("leveltools.enabled")) {
      return;
    }

    final Block block = e.getBlock();
    final ItemStack hand = LevelToolsUtil.getHand(player);

    if (!LevelToolsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks")) {
      final PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
      if (pdc.has(trackedBlocks)) {
        final List<Vector> blocks = pdc.get(trackedBlocks, DataType.asList(DataType.VECTOR));
        if (blocks != null && blocks.remove(block.getLocation().toVector())) {
          pdc.set(trackedBlocks, DataType.asList(DataType.VECTOR), blocks);
          return;
        }
      }
    }

    final String type =
        LevelToolsPlugin.getInstance().getConfig().getString("block_list_type", "blacklist");
    final Set<Material> blocks =
        LevelToolsPlugin.getInstance().getConfig().getStringList("block_list").stream()
            .map(Material::getMaterial)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    if (type != null && type.equalsIgnoreCase("whitelist") && !blocks.contains(block.getType())) {
      return;
    }

    if (type != null && type.equalsIgnoreCase("blacklist") && blocks.contains(block.getType())) {
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
