package me.byteful.plugin.leveltools.api.event;

import lombok.*;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class LevelToolsLevelIncreaseEvent extends Event implements Cancellable {
  private static final HandlerList handlers = new HandlerList();

  private final LevelToolsItem item;
  private final Player player;
  private int newLevel;
  private boolean isCancelled;

  @Nonnull
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  @Nonnull
  public HandlerList getHandlers() {
    return handlers;
  }
}
