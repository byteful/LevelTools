package me.byteful.plugin.leveltools.api.event;

import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LevelToolsLevelIncreaseEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();
  @NotNull private final LevelToolsItem item;
  @NotNull private final Player player;
  private int newLevel;
  private boolean isCancelled;

  public LevelToolsLevelIncreaseEvent(@NotNull LevelToolsItem item, @NotNull Player player) {
    this.item = item;
    this.player = player;
  }

  public LevelToolsLevelIncreaseEvent(
      @NotNull LevelToolsItem item, @NotNull Player player, int newLevel, boolean isCancelled) {
    this.item = item;
    this.player = player;
    this.newLevel = newLevel;
    this.isCancelled = isCancelled;
  }

  @NotNull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @NotNull
  public LevelToolsItem getItem() {
    return item;
  }

  @NotNull
  public Player getPlayer() {
    return player;
  }

  public int getNewLevel() {
    return newLevel;
  }

  public void setNewLevel(int newLevel) {
    this.newLevel = newLevel;
  }

  @Override
  public boolean isCancelled() {
    return isCancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    isCancelled = cancelled;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LevelToolsLevelIncreaseEvent that = (LevelToolsLevelIncreaseEvent) o;
    return newLevel == that.newLevel
        && isCancelled == that.isCancelled
        && item.equals(that.item)
        && player.equals(that.player);
  }

  @Override
  public int hashCode() {
    return Objects.hash(item, player, newLevel, isCancelled);
  }

  @Override
  @NotNull
  public HandlerList getHandlers() {
    return handlers;
  }

  @Override
  public String toString() {
    return "LevelToolsLevelIncreaseEvent{"
        + "item="
        + item
        + ", player="
        + player
        + ", newLevel="
        + newLevel
        + ", isCancelled="
        + isCancelled
        + '}';
  }
}
