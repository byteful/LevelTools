package me.byteful.plugin.leveltools.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import redempt.redlib.misc.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

// From: https://www.spigotmc.org/wiki/creating-an-update-checker-that-checks-for-updates
// Further modified by byteful to accompany LevelTools
public class UpdateChecker {
  public static final int SPIGOT_RESOURCE_ID = 97516;

  @NotNull
  private final JavaPlugin plugin;

  public UpdateChecker(@NotNull JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void check() {
    plugin.getLogger().info("Checking for updates...");
    Task.asyncDelayed(plugin, () -> {
      try (final InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + SPIGOT_RESOURCE_ID).openStream(); final Scanner scanner = new Scanner(inputStream)) {
        if (!scanner.hasNext()) {
          return;
        }

        final String currentVersion = plugin.getDescription().getVersion();
        if (currentVersion.contains("BETA")) {
          plugin.getLogger().info("Update check was cancelled because you are running a beta build!");

          return;
        }
        final String latestVersion = scanner.next();

        if (currentVersion.equals(latestVersion)) {
          plugin.getLogger().info("No new updates found.");
        } else {
          plugin.getLogger().info("A new update was found. You are on " + currentVersion + " while the latest version is " + latestVersion + ".");
          plugin.getLogger().info("Please install this update from: https://www.spigotmc.org/resources/" + SPIGOT_RESOURCE_ID);
        }
      } catch (IOException e) {
        plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
      }
    });
  }
}