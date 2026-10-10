package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BlockPlacementListener implements Listener {
    private final Scheduler scheduler;

    public BlockPlacementListener(@NotNull Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    private boolean isPPBEnabled() {
        return !LevelToolsPlugin.getInstance().getConfigManager().getSettings().isCountPlayerPlacedBlocks();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isPPBEnabled()) {
            return;
        }

        Block block = event.getBlock();
        untrack(block);

        // Breaking one half of a door, bed or tall plant also removes the other half, without its own event.
        Block otherHalf = getOtherHalf(block);
        if (otherHalf != null) {
            untrack(otherHalf);
        }
    }

    private void untrack(@NotNull Block block) {
        BlockPosition pos = BlockPosition.fromBukkit(block);
        if (!LevelToolsPlugin.getInstance().getBlockDataManager().isPlacedBlock(pos)) {
            return;
        }

        scheduler.locationDelayed(() -> LevelToolsPlugin.getInstance().getBlockDataManager().removePlacedBlock(pos), block.getLocation(), 1);
    }

    @Nullable
    private static Block getOtherHalf(@NotNull Block block) {
        if (!LevelToolsUtil.supportsBlockData()) {
            return null;
        }

        BlockData data = block.getBlockData();
        Block other = null;
        if (data instanceof Bed) {
            Bed bed = (Bed) data;
            other = block.getRelative(bed.getPart() == Bed.Part.HEAD ? bed.getFacing().getOppositeFace() : bed.getFacing());
        } else if (data instanceof Bisected && !(data instanceof Stairs) && !(data instanceof TrapDoor)) {
            other = block.getRelative(((Bisected) data).getHalf() == Bisected.Half.TOP ? BlockFace.DOWN : BlockFace.UP);
        }

        return other != null && other.getType() == block.getType() ? other : null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isPPBEnabled()) {
            return;
        }

        BlockDataManager blockDataManager = LevelToolsPlugin.getInstance().getBlockDataManager();
        if (event instanceof BlockMultiPlaceEvent) {
            // Doors, beds, tall flowers, etc. place more than one block; track every part of them.
            for (BlockState state : ((BlockMultiPlaceEvent) event).getReplacedBlockStates()) {
                blockDataManager.addPlacedBlock(BlockPosition.fromBukkit(state.getBlock()));
            }
            return;
        }

        blockDataManager.addPlacedBlock(BlockPosition.fromBukkit(event.getBlock()));
    }
}
