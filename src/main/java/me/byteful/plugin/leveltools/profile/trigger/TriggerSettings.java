package me.byteful.plugin.leveltools.profile.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TriggerSettings {
    private final ClickMode clickMode;

    private TriggerSettings(@Nullable ClickMode clickMode) {
        this.clickMode = clickMode;
    }

    @NotNull
    public static TriggerSettings empty() {
        return new TriggerSettings(null);
    }

    @NotNull
    public static TriggerSettings forClick(@NotNull ClickMode mode) {
        return new TriggerSettings(mode);
    }

    @Nullable
    public ClickMode getClickMode() {
        return clickMode;
    }

    public enum ClickMode {
        ANY,
        ON_BLOCK,
        ON_ENTITY,
        ON_AIR;

        @NotNull
        public static ClickMode fromString(@NotNull String value) {
            try {
                return valueOf(value.toUpperCase().replace("-", "_"));
            } catch (IllegalArgumentException e) {
                return ANY;
            }
        }
    }
}
