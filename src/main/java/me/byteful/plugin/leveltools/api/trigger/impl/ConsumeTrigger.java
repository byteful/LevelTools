package me.byteful.plugin.leveltools.api.trigger.impl;

import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.api.trigger.TriggerType;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ConsumeTrigger implements Trigger {
    @Override
    @NotNull
    public TriggerType getType() {
        return TriggerType.CONSUME;
    }

    @Override
    public boolean canHandle(@NotNull TriggerContext context) {
        ItemStack consumedItem = context.getSourceAs(ItemStack.class);
        if (consumedItem == null) {
            return false;
        }

        TriggerProfile profile = context.getTriggerProfile();
        String itemType = consumedItem.getType().name();

        return profile.isSourceAllowed(itemType);
    }

    @Override
    public double calculateXpModifier(@NotNull TriggerContext context) {
        ItemStack consumedItem = context.getSourceAs(ItemStack.class);
        if (consumedItem == null) {
            return 0;
        }

        TriggerProfile profile = context.getTriggerProfile();
        return profile.calculateXpModifier(consumedItem.getType().name());
    }

}
