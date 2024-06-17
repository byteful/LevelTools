package me.byteful.plugin.leveltools.api.scheduler.impl.bukkit;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.scheduler.ScheduledTask;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class BukkitScheduler implements Scheduler {
  private final LevelToolsPlugin plugin;

  public BukkitScheduler(LevelToolsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void asyncDelayed(Runnable runnable, long ticksDelay) {
    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, ticksDelay);
  }

  @Override
  public void syncTimer(Runnable runnable, long ticksDelay, long ticksPeriod) {
    Bukkit.getScheduler().runTaskTimer(plugin, runnable, ticksDelay, ticksPeriod);
  }

  @Override
  public void locationDelayed(Runnable runnable, Location location, long ticksDelay) {
    Bukkit.getScheduler().runTaskLater(plugin, runnable, ticksDelay);
  }

  @Override
  public ScheduledTask asyncTimer(Runnable runnable, long ticksDelay, long ticksPeriod) {
    return new BukkitScheduledTask(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, ticksDelay, ticksPeriod));
  }
}
