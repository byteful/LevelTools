package me.byteful.plugin.leveltools.api.scheduler.impl.folia;


import me.byteful.plugin.leveltools.api.scheduler.ScheduledTask;

public class FoliaScheduledTask implements ScheduledTask {
  private final io.papermc.paper.threadedregions.scheduler.ScheduledTask task;

  public FoliaScheduledTask(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
    this.task = task;
  }

  @Override
  public void stop() {
    task.cancel();
  }
}
