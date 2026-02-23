package me.byteful.plugin.leveltools.api.trigger.impl;

import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.api.trigger.TriggerIds;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ArmorDurabilityTrigger implements Trigger {
    @Override
    @NotNull
    public String getTriggerId() {
        return TriggerIds.ARMOR_DURABILITY;
    }

    @Override
    public boolean canHandle(@NotNull TriggerContext context) {
        Material armorMaterial = resolveArmorMaterial(context);
        if (armorMaterial == null) {
            return false;
        }

        TriggerProfile profile = context.getTriggerProfile();
        return profile.isSourceAllowed(armorMaterial.name());
    }

    @Override
    public double calculateXpModifier(@NotNull TriggerContext context) {
        Material armorMaterial = resolveArmorMaterial(context);
        if (armorMaterial == null) {
            return 0;
        }

        TriggerProfile profile = context.getTriggerProfile();
        return profile.calculateXpModifier(armorMaterial.name());
    }

    @Nullable
    private Material resolveArmorMaterial(@NotNull TriggerContext context) {
        Material sourceMaterial = context.getSourceAs(Material.class);
        if (sourceMaterial != null) {
            return sourceMaterial;
        }

        ItemStack sourceItem = context.getSourceAs(ItemStack.class);
        if (sourceItem != null) {
            return sourceItem.getType();
        }

        ItemStack contextItem = context.getItem();
        return contextItem.getType();
    }
}