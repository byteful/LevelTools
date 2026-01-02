package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.block.BlockDataManager;
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
    private final BlockDataManager blockDataManager;
    private final Scheduler scheduler;

    public BlockPlacementListener(@NotNull BlockDataManager blockDataManager, @NotNull Scheduler scheduler) {
        this.blockDataManager = blockDataManager;
        this.scheduler = scheduler;
    }

    private boolean isPPBEnabled() {
        return !LevelToolsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isPPBEnabled()) {
            return;
        }

        Block block = event.getBlock();
        BlockPosition pos = BlockPosition.fromBukkit(block);
        scheduler.locationDelayed(() -> blockDataManager.removePlacedBlock(pos), block.getLocation(), 1);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isPPBEnabled()) {
            return;
        }

        blockDataManager.addPlacedBlock(BlockPosition.fromBukkit(event.getBlock()));
    }
}
