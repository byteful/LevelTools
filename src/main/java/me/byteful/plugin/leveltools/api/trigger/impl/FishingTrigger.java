package me.byteful.plugin.leveltools.api.trigger.impl;

import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.api.trigger.TriggerType;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class FishingTrigger implements Trigger {
    @Override
    @NotNull
    public TriggerType getType() {
        return TriggerType.FISHING;
    }

    @Override
    public boolean canHandle(@NotNull TriggerContext context) {
        Entity caught = context.getSourceAs(Entity.class);
        if (!(caught instanceof Item)) {
            return false;
        }

        ItemStack itemStack = ((Item) caught).getItemStack();
        Material material = itemStack.getType();

        TriggerProfile profile = context.getTriggerProfile();
        return profile.isSourceAllowed(material.name());
    }

    @Override
    public double calculateXpModifier(@NotNull TriggerContext context) {
        Entity caught = context.getSourceAs(Entity.class);
        if (!(caught instanceof Item)) {
            return 0;
        }

        ItemStack itemStack = ((Item) caught).getItemStack();
        Material material = itemStack.getType();

        TriggerProfile profile = context.getTriggerProfile();
        return profile.calculateXpModifier(material.name());
    }
}
