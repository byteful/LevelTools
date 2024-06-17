package me.byteful.plugin.leveltools.api.scheduler.impl.bukkit;

import me.byteful.plugin.leveltools.api.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

public class BukkitScheduledTask implements ScheduledTask {
  private final BukkitTask task;

  public BukkitScheduledTask(BukkitTask task) {
    this.task = task;
  }

  @Override
  public void stop() {
    task.cancel();
  }
}
