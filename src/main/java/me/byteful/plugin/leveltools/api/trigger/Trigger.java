package me.byteful.plugin.leveltools.api.trigger;

import org.jetbrains.annotations.NotNull;

public interface Trigger {
    @NotNull
    TriggerType getType();

    boolean canHandle(@NotNull TriggerContext context);

    double calculateXpModifier(@NotNull TriggerContext context);
}
