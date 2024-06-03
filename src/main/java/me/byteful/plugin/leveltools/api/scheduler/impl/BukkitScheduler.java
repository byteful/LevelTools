package me.byteful.plugin.leveltools.api.scheduler.impl;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import org.bukkit.Bukkit;

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
}
