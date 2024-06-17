package me.byteful.plugin.leveltools.api.scheduler;

import org.bukkit.Location;

public interface Scheduler {
  void asyncDelayed(Runnable runnable, long ticksDelay);

  void syncTimer(Runnable runnable, long ticksDelay, long ticksPeriod);

  void locationDelayed(Runnable runnable, Location location, long ticksDelay);

  ScheduledTask asyncTimer(Runnable runnable, long ticksDelay, long ticksPeriod);
}
