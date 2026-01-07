package me.byteful.plugin.leveltools.api.trigger;

import org.jetbrains.annotations.NotNull;

public interface Trigger {
    @NotNull
    String getTriggerId();

    boolean canHandle(@NotNull TriggerContext context);

    double calculateXpModifier(@NotNull TriggerContext context);
}
