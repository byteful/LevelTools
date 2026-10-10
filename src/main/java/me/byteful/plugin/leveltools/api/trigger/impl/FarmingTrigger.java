package me.byteful.plugin.leveltools.api.trigger.impl;

import com.cryptomorin.xseries.XMaterial;
import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.api.trigger.TriggerIds;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;
import org.bukkit.material.NetherWarts;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public final class FarmingTrigger implements Trigger {
    private static final Set<XMaterial> TILLABLE_BLOCKS = EnumSet.of(
            XMaterial.DIRT,
            XMaterial.GRASS_BLOCK,
            XMaterial.DIRT_PATH,
            XMaterial.COARSE_DIRT,
            XMaterial.ROOTED_DIRT
    );

    // Crops without a meaningful growth age: harvesting them is always a full harvest.
    // Sugar cane, cactus, kelp and bamboo do have an age, but it resets as they grow taller.
    private static final Set<XMaterial> AGELESS_CROPS = EnumSet.of(
            XMaterial.MELON,
            XMaterial.PUMPKIN,
            XMaterial.SUGAR_CANE,
            XMaterial.CACTUS,
            XMaterial.BAMBOO,
            XMaterial.KELP,
            XMaterial.KELP_PLANT,
            XMaterial.BROWN_MUSHROOM,
            XMaterial.RED_MUSHROOM
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
            if (!isTillable(block)) {
                return false;
            }
            return profile.isSourceAllowed("TILL");
        }

        if (context.getOriginalEventAs(BlockBreakEvent.class) != null) {
            if (!LevelToolsUtil.supportsBlockData()) {
                MaterialData data = block.getState().getData();

                if (data instanceof Crops) {
                    Crops crop = (Crops) data;
                    if (crop.getState() != CropState.RIPE) {
                        return false;
                    }
                } else if (data instanceof NetherWarts) {
                    NetherWarts wart = (NetherWarts) data;
                    if (wart.getState() != NetherWartsState.RIPE) {
                        return false;
                    }
                } else if (data instanceof CocoaPlant) {
                    CocoaPlant cocoa = (CocoaPlant) data;
                    if (cocoa.getSize() != CocoaPlant.CocoaPlantSize.LARGE) {
                        return false;
                    }
                }
            } else if (!isAgelessCrop(block)) {
                BlockData data = block.getBlockData();

                if (data instanceof Ageable) {
                    Ageable ageable = (Ageable) data;
                    if (ageable.getAge() < ageable.getMaximumAge()) {
                        return false;
                    }
                } else {
                    return false;
                }
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

    public static boolean isTillable(@NotNull Block block) {
        return matches(TILLABLE_BLOCKS, block);
    }

    private static boolean isAgelessCrop(@NotNull Block block) {
        return matches(AGELESS_CROPS, block);
    }

    // Uses the Optional lookup so blocks newer than XSeries don't throw.
    private static boolean matches(@NotNull Set<XMaterial> materials, @NotNull Block block) {
        return XMaterial.matchXMaterial(block.getType().name()).map(materials::contains).orElse(false);
    }

    public static boolean isMatureCropSource(@NotNull Block block) {
        if (!LevelToolsUtil.supportsBlockData()) {
            final MaterialData data = block.getState().getData();

            if (data instanceof Crops) {
                final Crops crop = (Crops) data;
                return crop.getState() == CropState.RIPE;
            }

            if (data instanceof NetherWarts) {
                final NetherWarts wart = (NetherWarts) data;
                return wart.getState() == NetherWartsState.RIPE;
            }

            if (data instanceof CocoaPlant) {
                final CocoaPlant cocoa = (CocoaPlant) data;
                return cocoa.getSize() == CocoaPlant.CocoaPlantSize.LARGE;
            }

            return false;
        }

        // Player-placed melons, pumpkins, etc. are the placed block itself, so they never count as grown.
        if (isAgelessCrop(block)) {
            return false;
        }

        final BlockData data = block.getBlockData();
        if (data instanceof Ageable) {
            final Ageable ageable = (Ageable) data;
            return ageable.getAge() >= ageable.getMaximumAge();
        }

        return false;
    }
}
