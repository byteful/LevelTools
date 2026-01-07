package me.byteful.plugin.leveltools.api.trigger;

import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTrigger implements Trigger {
    private final String triggerId;

    protected AbstractTrigger(@NotNull String triggerId) {
        this.triggerId = TriggerIds.normalize(triggerId);
        if (this.triggerId.isEmpty()) {
            throw new IllegalArgumentException("Trigger ID cannot be null or empty");
        }
    }

    @Override
    @NotNull
    public final String getTriggerId() {
        return triggerId;
    }

    protected double calculateDefaultXpModifier(@NotNull TriggerContext context, @NotNull String sourceKey) {
        TriggerProfile profile = context.getTriggerProfile();
        return profile.calculateXpModifier(sourceKey);
    }

    protected boolean isSourceAllowed(@NotNull TriggerContext context, @NotNull String sourceKey) {
        TriggerProfile profile = context.getTriggerProfile();
        return profile.isSourceAllowed(sourceKey);
    }
}
