package me.byteful.plugin.leveltools.api.trigger.impl;

import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.api.trigger.TriggerIds;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public final class FarmingTrigger implements Trigger {
    private static final Set<Material> TILLABLE_BLOCKS = EnumSet.of(
            Material.DIRT,
            Material.GRASS_BLOCK,
            Material.DIRT_PATH,
            Material.COARSE_DIRT,
            Material.ROOTED_DIRT
    );

    @Override
    @NotNull
    public String getTriggerId() {
        return TriggerIds.FARMING;
    }

    @Override
    public boolean canHandle(@NotNull TriggerContext context) {
        Block block = context.getSourceAs(Block.class);
        if (block == null) {
            return false;
        }

        TriggerProfile profile = context.getTriggerProfile();
        Material blockType = block.getType();

        if (context.getOriginalEventAs(PlayerInteractEvent.class) != null) {
            if (!TILLABLE_BLOCKS.contains(blockType)) {
                return false;
            }
            return profile.isSourceAllowed("TILL");
        }

        if (context.getOriginalEventAs(BlockBreakEvent.class) != null) {
            BlockData data = block.getBlockData();
            if (data instanceof Ageable) {
                Ageable ageable = (Ageable) data;
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    return false;
                }
            } else {
                return false;
            }

            return profile.isSourceAllowed(blockType.name());
        }

        return false;
    }

    @Override
    public double calculateXpModifier(@NotNull TriggerContext context) {
        Block block = context.getSourceAs(Block.class);
        if (block == null) {
            return 0;
        }

        TriggerProfile profile = context.getTriggerProfile();

        if (context.getOriginalEventAs(PlayerInteractEvent.class) != null) {
            return profile.calculateXpModifier("TILL");
        }

        return profile.calculateXpModifier(block.getType().name());
    }
}
