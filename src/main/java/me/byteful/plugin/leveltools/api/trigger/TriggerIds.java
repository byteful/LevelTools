package me.byteful.plugin.leveltools.api.trigger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TriggerIds {
    public static final String BLOCK_BREAK = "block_break";
    public static final String ENTITY_KILL = "entity_kill";
    public static final String FISHING = "fishing";
    public static final String RIGHT_CLICK = "right_click";
    public static final String LEFT_CLICK = "left_click";
    public static final String CONSUME = "consume";

    private TriggerIds() {}

    @Nullable
    public static String normalize(@Nullable String id) {
        if (id == null) return null;
        return id.toLowerCase().replace("-", "_").trim();
    }

    public static boolean isBuiltIn(@NotNull String id) {
        String normalized = normalize(id);
        return BLOCK_BREAK.equals(normalized)
                || ENTITY_KILL.equals(normalized)
                || FISHING.equals(normalized)
                || RIGHT_CLICK.equals(normalized)
                || LEFT_CLICK.equals(normalized)
                || CONSUME.equals(normalized);
    }
}
