package me.byteful.plugin.leveltools.profile.reward;

import me.byteful.plugin.leveltools.api.RewardType;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class RewardEntry {
    private final RewardType type;
    private final String[] arguments;
    private final String rawString;

    public RewardEntry(@NotNull RewardType type, @NotNull String[] arguments, @NotNull String rawString) {
        this.type = type;
        this.arguments = arguments.clone();
        this.rawString = rawString;
    }

    @NotNull
    public RewardType getType() {
        return type;
    }

    @NotNull
    public String[] getArguments() {
        return arguments.clone();
    }

    @NotNull
    public String getRawString() {
        return rawString;
    }

    public void apply(@NotNull LevelToolsItem tool, @NotNull Player player) {
        type.apply(tool, arguments, player);
    }

    public boolean shouldUpdateItem() {
        return type.isShouldUpdate();
    }

    @Override
    public String toString() {
        return rawString;
    }
}
