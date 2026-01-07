package me.byteful.plugin.leveltools.api.trigger.impl;

import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.api.trigger.TriggerIds;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public final class EntityKillTrigger implements Trigger {
    @Override
    @NotNull
    public String getTriggerId() {
        return TriggerIds.ENTITY_KILL;
    }

    @Override
    public boolean canHandle(@NotNull TriggerContext context) {
        Entity entity = context.getSourceAs(Entity.class);
        if (entity == null) {
            return false;
        }

        TriggerProfile profile = context.getTriggerProfile();
        String entityType = entity.getType().name();

        return profile.isSourceAllowed(entityType);
    }

    @Override
    public double calculateXpModifier(@NotNull TriggerContext context) {
        Entity entity = context.getSourceAs(Entity.class);
        if (entity == null) {
            return 0;
        }

        TriggerProfile profile = context.getTriggerProfile();
        return profile.calculateXpModifier(entity.getType().name());
    }

}
