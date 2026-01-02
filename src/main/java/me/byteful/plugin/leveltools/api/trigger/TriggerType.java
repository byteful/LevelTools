package me.byteful.plugin.leveltools.api.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TriggerType {
    BLOCK_BREAK("block_break"),
    ENTITY_KILL("entity_kill"),
    FISHING("fishing"),
    RIGHT_CLICK("right_click"),
    LEFT_CLICK("left_click"),
    CONSUME("consume");

    private final String configKey;

    TriggerType(@NotNull String configKey) {
        this.configKey = configKey;
    }

    @Nullable
    public static TriggerType fromConfigKey(@NotNull String key) {
        String normalized = key.toLowerCase().replace("-", "_");
        for (TriggerType type : values()) {
            if (type.configKey.equals(normalized)) {
                return type;
            }
        }
        return null;
    }

    @Nullable
    public static TriggerType fromString(@NotNull String value) {
        try {
            return valueOf(value.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return fromConfigKey(value);
        }
    }

    @NotNull
    public String getConfigKey() {
        return configKey;
    }
}
