package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import redempt.redlib.blockdata.DataBlock;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockEventListener extends XPListener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        final Player player = e.getPlayer();

        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        final Block block = e.getBlock();
        final ItemStack hand = LevelToolsUtil.getHand(player);

        if (!LevelToolsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks")) {
            final DataBlock db = LevelToolsPlugin.getInstance().getBlockDataManager().getDataBlock(block, false);

            if (db != null && db.contains("level_tools") && db.getBoolean("level_tools")) {
                return;
            }
        }

        final String type = LevelToolsPlugin.getInstance().getConfig().getString("block_list_type", "blacklist");
        final Set<Material> blocks = LevelToolsPlugin.getInstance().getConfig().getStringList("block_list").stream().map(Material::getMaterial).filter(Objects::nonNull).collect(Collectors.toSet());

        if (type != null && type.equalsIgnoreCase("whitelist") && !blocks.contains(block.getType())) {
            return;
        }

        if (type != null && type.equalsIgnoreCase("blacklist") && blocks.contains(block.getType())) {
            return;
        }

        if (!LevelToolsUtil.isAxe(hand.getType()) && !LevelToolsUtil.isPickaxe(hand.getType()) && !LevelToolsUtil.isShovel(hand.getType())) {
            return;
        }

        handle(LevelToolsUtil.createLevelToolsItem(hand), player, LevelToolsUtil.getBlockModifier(block.getType()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!LevelToolsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks")) {
            LevelToolsPlugin.getInstance().getBlockDataManager().getDataBlockAsync(e.getBlockPlaced(), true).thenAccept(db -> db.set("level_tools", true));
        }
    }
}
