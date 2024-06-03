package me.byteful.plugin.leveltools.api.scheduler.impl;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class FoliaScheduler implements Scheduler {
  private final LevelToolsPlugin plugin;

  public FoliaScheduler(LevelToolsPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void asyncDelayed(Runnable runnable, long ticksDelay) {
    Bukkit.getAsyncScheduler().runDelayed(plugin, x -> runnable.run(), ticksDelay * 50, TimeUnit.MILLISECONDS);
  }

  @Override
  public void syncTimer(Runnable runnable, long ticksDelay, long ticksPeriod) {
    Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, x -> runnable.run(), ticksDelay, ticksPeriod);
  }
}
