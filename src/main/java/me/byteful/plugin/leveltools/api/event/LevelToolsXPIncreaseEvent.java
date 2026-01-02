package me.byteful.plugin.leveltools.api.event;

import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LevelToolsXPIncreaseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @NotNull
    private final LevelToolsItem item;
    @NotNull
    private final Player player;
    private final double initialXp;
    private double newXp;
    private boolean isCancelled, isModified = false;

    public LevelToolsXPIncreaseEvent(
            @NotNull LevelToolsItem item, @NotNull Player player, double initialXp) {
        this.item = item;
        this.player = player;
        this.initialXp = initialXp;
    }

    public LevelToolsXPIncreaseEvent(
            @NotNull LevelToolsItem item,
            @NotNull Player player,
            double initialXp,
            double newXp,
            boolean isCancelled) {
        this.item = item;
        this.player = player;
        this.initialXp = initialXp;
        this.newXp = newXp;
        this.isCancelled = isCancelled;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        isCancelled = cancelled;
    }

    @NotNull
    public LevelToolsItem getItem() {
        return item;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public double getNewXp() {
        return newXp;
    }

    public void setNewXp(double newXp) {
        this.newXp = newXp;
        this.isModified = true;
    }

    public double getInitialXp() {
        return initialXp;
    }

    public boolean isModified() {
        return isModified;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LevelToolsXPIncreaseEvent that = (LevelToolsXPIncreaseEvent) object;
        return Double.compare(getInitialXp(), that.getInitialXp()) == 0
                && Double.compare(getNewXp(), that.getNewXp()) == 0
                && isCancelled() == that.isCancelled()
                && Objects.equals(getItem(), that.getItem())
                && Objects.equals(getPlayer(), that.getPlayer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItem(), getPlayer(), getInitialXp(), getNewXp(), isCancelled());
    }

    @Override
    public String toString() {
        return "LevelToolsXPIncreaseEvent{"
                + "item="
                + item
                + ", player="
                + player
                + ", initialXp="
                + initialXp
                + ", newXp="
                + newXp
                + ", isCancelled="
                + isCancelled
                + '}';
    }
}
