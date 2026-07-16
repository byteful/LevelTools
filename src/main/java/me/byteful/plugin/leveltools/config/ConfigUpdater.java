package me.byteful.plugin.leveltools.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public final class ConfigUpdater {
    private static final String DEFAULT_FORMULA = "100 + {current_level} * 100";

    private final Path dataFolder;
    private final Logger logger;

    public ConfigUpdater(@NotNull Path dataFolder, @NotNull Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    public void updateMainConfig() {
        Path configPath = dataFolder.resolve("config.yml");
        if (!Files.exists(configPath)) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configPath.toFile());
        boolean changed = false;

        if (!config.contains("xp_formulas.global")) {
            config.set("xp_formulas.global", config.getString("level_xp_formula", DEFAULT_FORMULA));
            changed = true;
        }

        if (config.contains("level_xp_formula")) {
            config.set("level_xp_formula", null);
            changed = true;
        }

        if (!config.contains("prevent_enchanted_books_on_leveltools_items")) {
            config.set("prevent_enchanted_books_on_leveltools_items", false);
            changed = true;
        }

        if (!config.contains("farming.ignore_player_placed_blocks_for_fully_grown_crops")) {
            config.set("farming.ignore_player_placed_blocks_for_fully_grown_crops", true);
            changed = true;
        }

        if (!config.contains("force_nbt")) {
            config.set("force_nbt", false);
            changed = true;
        }

        if (!changed) {
            return;
        }

        backupConfig(configPath);
        try {
            config.save(configPath.toFile());
            logger.info("Updated config.yml with v2.2.0 configuration keys.");
        } catch (IOException e) {
            logger.severe("Failed to update config.yml: " + e.getMessage());
        }
    }

    private void backupConfig(@NotNull Path configPath) {
        Path backupPath = dataFolder.resolve("config-v2.2-backup-" + System.currentTimeMillis() + ".yml");
        try {
            Files.copy(configPath, backupPath, StandardCopyOption.COPY_ATTRIBUTES);
            logger.info("Backed up config.yml to " + backupPath.getFileName());
        } catch (IOException e) {
            logger.warning("Failed to backup config.yml before updating: " + e.getMessage());
        }
    }
}