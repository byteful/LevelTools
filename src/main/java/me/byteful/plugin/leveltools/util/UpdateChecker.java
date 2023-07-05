package me.byteful.plugin.leveltools.util;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import org.jetbrains.annotations.NotNull;
import redempt.redlib.misc.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

public class UpdateChecker {
    @NotNull
    private final LevelToolsPlugin plugin;

    public UpdateChecker(@NotNull LevelToolsPlugin plugin) {
        this.plugin = plugin;
    }

    public void check() {
        plugin.getLogger().info("Checking for updates...");
        final String currentVersion = plugin.getDescription().getVersion();
        if (currentVersion.contains("BETA")) {
            plugin.getLogger().info("Update check was cancelled because you are running a beta build!");

            return;
        }

        Task.asyncDelayed(plugin, () -> {
            try (final InputStream inputStream = new URL("https://api.byteful.me/leveltools").openStream(); final Scanner scanner = new Scanner(inputStream)) {
                if (!scanner.hasNext()) {
                    return;
                }

                final String latestVersion = scanner.next();

                if (currentVersion.equals(latestVersion)) {
                    plugin.getLogger().info("No new updates found.");
                } else {
                    plugin.getLogger().info("A new update was found. You are on " + currentVersion + " while the latest version is " + latestVersion + ".");
                    plugin.getLogger().info("Please install this update from: https://github.com/byteful/LevelTools/releases/download/v" + latestVersion + "/LevelTools-" + latestVersion + ".jar");
                }
            } catch (IOException e) {
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
            }
        });
    }
}
