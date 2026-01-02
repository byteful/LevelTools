package me.byteful.plugin.leveltools.config.migration;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public final class ConfigMigrator {
    private final LevelToolsPlugin plugin;
    private final Path dataFolder;
    private final Logger logger;

    public ConfigMigrator(@NotNull LevelToolsPlugin plugin, @NotNull Path dataFolder, @NotNull Logger logger) {
        this.plugin = plugin;
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    public void migrate() {
        Path oldConfigPath = dataFolder.resolve("config.yml");
        Path backupPath = dataFolder.resolve("old_config.yml");

        if (!Files.exists(oldConfigPath)) {
            logger.warning("No config.yml found to migrate.");
            return;
        }

        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigPath.toFile());

        backupOldConfig(oldConfigPath, backupPath);

        V1ToV2Migration migration = new V1ToV2Migration(oldConfig, dataFolder, logger);
        migration.execute();
    }

    private void backupOldConfig(@NotNull Path configPath, @NotNull Path backupPath) {
        try {
            Files.move(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Backed up old config.yml to old_config.yml");
        } catch (IOException e) {
            logger.severe("Failed to backup old config: " + e.getMessage());
        }
    }
}
