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
  private double newXp;
  private boolean isCancelled;

  public LevelToolsXPIncreaseEvent(@NotNull LevelToolsItem item, @NotNull Player player) {
    this.item = item;
    this.player = player;
  }

  public LevelToolsXPIncreaseEvent(
      @NotNull LevelToolsItem item, @NotNull Player player, double newXp, boolean isCancelled) {
    this.item = item;
    this.player = player;
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
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LevelToolsXPIncreaseEvent that = (LevelToolsXPIncreaseEvent) o;
    return Double.compare(that.newXp, newXp) == 0
        && isCancelled == that.isCancelled
        && item.equals(that.item)
        && player.equals(that.player);
  }

  @Override
  public int hashCode() {
    return Objects.hash(item, player, newXp, isCancelled);
  }
}
