package me.byteful.plugin.leveltools.config;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.config.migration.ConfigMigrator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public final class ConfigManager {
    private final LevelToolsPlugin plugin;
    private final Path dataFolder;
    private final Logger logger;

    private FileConfiguration mainConfig;
    private FileConfiguration triggerProfilesConfig;
    private FileConfiguration rewardProfilesConfig;
    private FileConfiguration displayProfilesConfig;
    private FileConfiguration itemProfilesConfig;

    public ConfigManager(@NotNull LevelToolsPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder().toPath();
        this.logger = plugin.getLogger();
    }

    public void loadAll() {
        ensureDataFolder();

        if (needsMigration()) {
            logger.info("Detected v1 configuration. Starting migration to v2...");
            ConfigMigrator migrator = new ConfigMigrator(plugin, dataFolder, logger);
            migrator.migrate();
            logger.info("Migration complete!");
        }

        mainConfig = loadOrCreate("config.yml");
        triggerProfilesConfig = loadOrCreate("trigger_profiles.yml");
        rewardProfilesConfig = loadOrCreate("reward_profiles.yml");
        displayProfilesConfig = loadOrCreate("display_profiles.yml");
        itemProfilesConfig = loadOrCreate("item_profiles.yml");

        logger.info("Loaded all configuration files.");
    }

    public void reload() {
        loadAll();
    }

    private void ensureDataFolder() {
        try {
            if (!Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }
        } catch (IOException e) {
            logger.severe("Failed to create data folder: " + e.getMessage());
        }
    }

    private boolean needsMigration() {
        Path configPath = dataFolder.resolve("config.yml");
        if (!Files.exists(configPath)) {
            return false;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configPath.toFile());
        return config.contains("tool_rewards") || config.contains("sword_rewards") || config.contains("bow_rewards");
    }

    @NotNull
    private FileConfiguration loadOrCreate(@NotNull String filename) {
        File file = dataFolder.resolve(filename).toFile();

        if (!file.exists()) {
            plugin.saveResource(filename, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        InputStream defaultStream = plugin.getResource(filename);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            config.setDefaults(defaultConfig);
        }

        return config;
    }

    public void saveConfig(@NotNull FileConfiguration config, @NotNull String filename) {
        try {
            config.save(dataFolder.resolve(filename).toFile());
        } catch (IOException e) {
            logger.severe("Failed to save " + filename + ": " + e.getMessage());
        }
    }

    @NotNull
    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    @NotNull
    public FileConfiguration getTriggerProfilesConfig() {
        return triggerProfilesConfig;
    }

    @NotNull
    public FileConfiguration getRewardProfilesConfig() {
        return rewardProfilesConfig;
    }

    @NotNull
    public FileConfiguration getDisplayProfilesConfig() {
        return displayProfilesConfig;
    }

    @NotNull
    public FileConfiguration getItemProfilesConfig() {
        return itemProfilesConfig;
    }
}
