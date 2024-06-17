package me.byteful.plugin.leveltools.api.scheduler;

public interface Scheduler {
  void asyncDelayed(Runnable runnable, long ticksDelay);

  void syncTimer(Runnable runnable, long ticksDelay, long ticksPeriod);
}
