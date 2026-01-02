package me.byteful.plugin.leveltools.profile.trigger;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public final class TriggerFilter {
    private final boolean whitelist;
    private final Set<String> entries;

    public TriggerFilter(boolean whitelist, @NotNull Set<String> entries) {
        this.whitelist = whitelist;
        this.entries = Collections.unmodifiableSet(entries);
    }

    @NotNull
    public static TriggerFilter empty() {
        return new TriggerFilter(false, Collections.emptySet());
    }

    public boolean isWhitelist() {
        return whitelist;
    }

    public boolean isBlacklist() {
        return !whitelist;
    }

    @NotNull
    public Set<String> getEntries() {
        return entries;
    }

    public boolean isAllowed(@NotNull String entry) {
        boolean inList = entries.contains(entry);
        return whitelist == inList;
    }
}
