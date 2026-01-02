package me.byteful.plugin.leveltools.api.trigger;

import me.byteful.plugin.leveltools.api.trigger.impl.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class TriggerRegistry {
    private final Map<TriggerType, Trigger> triggers = new EnumMap<>(TriggerType.class);

    public void register(@NotNull Trigger trigger) {
        triggers.put(trigger.getType(), trigger);
    }

    public void unregister(@NotNull TriggerType type) {
        triggers.remove(type);
    }

    @Nullable
    public Trigger get(@NotNull TriggerType type) {
        return triggers.get(type);
    }

    @NotNull
    public Optional<Trigger> getOptional(@NotNull TriggerType type) {
        return Optional.ofNullable(triggers.get(type));
    }

    public boolean has(@NotNull TriggerType type) {
        return triggers.containsKey(type);
    }

    @NotNull
    public Collection<Trigger> getAll() {
        return Collections.unmodifiableCollection(triggers.values());
    }

    public void clear() {
        triggers.clear();
    }

    public void registerDefaults() {
        register(new BlockBreakTrigger());
        register(new EntityKillTrigger());
        register(new FishingTrigger());
        register(new RightClickTrigger());
        register(new LeftClickTrigger());
        register(new ConsumeTrigger());
    }
}
