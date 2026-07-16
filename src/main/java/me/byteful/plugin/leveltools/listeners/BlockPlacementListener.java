package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

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
        BlockPosition pos = BlockPosition.fromBukkit(block);
        if (!LevelToolsPlugin.getInstance().getBlockDataManager().isPlacedBlock(pos)) {
            return;
        }

        scheduler.locationDelayed(() -> LevelToolsPlugin.getInstance().getBlockDataManager().removePlacedBlock(pos), block.getLocation(), 1);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isPPBEnabled()) {
            return;
        }

        LevelToolsPlugin.getInstance().getBlockDataManager().addPlacedBlock(BlockPosition.fromBukkit(event.getBlock()));
    }
}
