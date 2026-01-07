package me.byteful.plugin.leveltools.api.trigger;

import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TriggerContext {
    private final Player player;
    private final ItemStack item;
    private final String triggerId;
    private final Object source;
    private final Event originalEvent;
    private final ItemProfile itemProfile;
    private final TriggerProfile triggerProfile;

    private TriggerContext(
            @NotNull Player player,
            @NotNull ItemStack item,
            @NotNull String triggerId,
            @Nullable Object source,
            @NotNull Event originalEvent,
            @NotNull ItemProfile itemProfile,
            @NotNull TriggerProfile triggerProfile
    ) {
        this.player = player;
        this.item = item;
        this.triggerId = triggerId;
        this.source = source;
        this.originalEvent = originalEvent;
        this.itemProfile = itemProfile;
        this.triggerProfile = triggerProfile;
    }

    public static Builder builder() {
        return new Builder();
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @NotNull
    public String getTriggerId() {
        return triggerId;
    }

    @Nullable
    public Object getSource() {
        return source;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getSourceAs(@NotNull Class<T> type) {
        if (type.isInstance(source)) {
            return (T) source;
        }
        return null;
    }

    @NotNull
    public Event getOriginalEvent() {
        return originalEvent;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Event> T getOriginalEventAs(@NotNull Class<T> type) {
        if (type.isInstance(originalEvent)) {
            return (T) originalEvent;
        }
        return null;
    }

    @NotNull
    public ItemProfile getItemProfile() {
        return itemProfile;
    }

    @NotNull
    public TriggerProfile getTriggerProfile() {
        return triggerProfile;
    }

    public static final class Builder {
        private Player player;
        private ItemStack item;
        private String triggerId;
        private Object source;
        private Event originalEvent;
        private ItemProfile itemProfile;
        private TriggerProfile triggerProfile;

        private Builder() {
        }

        public Builder player(@NotNull Player player) {
            this.player = player;
            return this;
        }

        public Builder item(@NotNull ItemStack item) {
            this.item = item;
            return this;
        }

        public Builder triggerId(@NotNull String triggerId) {
            this.triggerId = TriggerIds.normalize(triggerId);
            return this;
        }

        public Builder source(@Nullable Object source) {
            this.source = source;
            return this;
        }

        public Builder originalEvent(@NotNull Event originalEvent) {
            this.originalEvent = originalEvent;
            return this;
        }

        public Builder itemProfile(@NotNull ItemProfile itemProfile) {
            this.itemProfile = itemProfile;
            return this;
        }

        public Builder triggerProfile(@NotNull TriggerProfile triggerProfile) {
            this.triggerProfile = triggerProfile;
            return this;
        }

        public TriggerContext build() {
            if (player == null) throw new IllegalStateException("Player must be set");
            if (item == null) throw new IllegalStateException("Item must be set");
            if (triggerId == null) throw new IllegalStateException("TriggerId must be set");
            if (originalEvent == null) throw new IllegalStateException("Original event must be set");
            if (itemProfile == null) throw new IllegalStateException("ItemProfile must be set");
            if (triggerProfile == null) throw new IllegalStateException("TriggerProfile must be set");

            return new TriggerContext(player, item, triggerId, source, originalEvent, itemProfile, triggerProfile);
        }
    }
}
