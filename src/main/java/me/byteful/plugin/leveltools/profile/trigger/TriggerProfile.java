package me.byteful.plugin.leveltools.profile.trigger;

import me.byteful.plugin.leveltools.api.trigger.TriggerType;
import org.jetbrains.annotations.NotNull;

public final class TriggerProfile {
    private final String id;
    private final TriggerType type;
    private final XpModifierConfig xpModifier;
    private final TriggerFilter filter;
    private final TriggerSettings settings;

    public TriggerProfile(
            @NotNull String id,
            @NotNull TriggerType type,
            @NotNull XpModifierConfig xpModifier,
            @NotNull TriggerFilter filter,
            @NotNull TriggerSettings settings
    ) {
        this.id = id;
        this.type = type;
        this.xpModifier = xpModifier;
        this.filter = filter;
        this.settings = settings;
    }

    public static Builder builder(@NotNull String id) {
        return new Builder(id);
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public TriggerType getType() {
        return type;
    }

    @NotNull
    public XpModifierConfig getXpModifier() {
        return xpModifier;
    }

    @NotNull
    public TriggerFilter getFilter() {
        return filter;
    }

    @NotNull
    public TriggerSettings getSettings() {
        return settings;
    }

    public double calculateXpModifier(@NotNull String source) {
        return xpModifier.calculateModifier(source);
    }

    public boolean isSourceAllowed(@NotNull String source) {
        return filter.isAllowed(source);
    }

    public static final class Builder {
        private final String id;
        private TriggerType type;
        private XpModifierConfig xpModifier;
        private TriggerFilter filter = TriggerFilter.empty();
        private TriggerSettings settings = TriggerSettings.empty();

        private Builder(@NotNull String id) {
            this.id = id;
        }

        public Builder type(@NotNull TriggerType type) {
            this.type = type;
            return this;
        }

        public Builder xpModifier(@NotNull XpModifierConfig xpModifier) {
            this.xpModifier = xpModifier;
            return this;
        }

        public Builder filter(@NotNull TriggerFilter filter) {
            this.filter = filter;
            return this;
        }

        public Builder settings(@NotNull TriggerSettings settings) {
            this.settings = settings;
            return this;
        }

        public TriggerProfile build() {
            if (type == null) {
                throw new IllegalStateException("TriggerType must be set");
            }
            if (xpModifier == null) {
                throw new IllegalStateException("XpModifierConfig must be set");
            }
            return new TriggerProfile(id, type, xpModifier, filter, settings);
        }
    }
}
