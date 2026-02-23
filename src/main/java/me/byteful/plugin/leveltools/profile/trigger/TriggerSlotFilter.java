package me.byteful.plugin.leveltools.profile.trigger;

import me.byteful.plugin.leveltools.api.trigger.TriggerSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public final class TriggerSlotFilter {
    private static final TriggerSlotFilter ALL = new TriggerSlotFilter(false, Collections.emptySet());

    private final boolean restricted;
    private final Set<TriggerSlot> slots;

    private TriggerSlotFilter(boolean restricted, @NotNull Set<TriggerSlot> slots) {
        this.restricted = restricted;
        this.slots = Collections.unmodifiableSet(slots);
    }

    @NotNull
    public static TriggerSlotFilter all() {
        return ALL;
    }

    @NotNull
    public static TriggerSlotFilter restricted(@NotNull Set<TriggerSlot> slots) {
        if (slots.isEmpty()) {
            return all();
        }
        return new TriggerSlotFilter(true, EnumSet.copyOf(slots));
    }

    public boolean isRestricted() {
        return restricted;
    }

    @NotNull
    public Set<TriggerSlot> getSlots() {
        return slots;
    }

    public boolean matches(@Nullable TriggerSlot slot) {
        if (!restricted) {
            return true;
        }
        if (slot == null) {
            return true;
        }
        return slots.contains(slot);
    }
}