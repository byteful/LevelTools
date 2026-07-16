package me.byteful.plugin.leveltools.config;

import com.cryptomorin.xseries.XSound;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.config.migration.ConfigMigrator;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
    private Settings settings;

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

        new ConfigUpdater(dataFolder, logger).updateMainConfig();

        mainConfig = loadOrCreate("config.yml");
        triggerProfilesConfig = loadOrCreate("trigger_profiles.yml");
        rewardProfilesConfig = loadOrCreate("reward_profiles.yml");
        displayProfilesConfig = loadOrCreate("display_profiles.yml");
        itemProfilesConfig = loadOrCreate("item_profiles.yml");
        settings = Settings.from(mainConfig, logger);

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

    @NotNull
    public Settings getSettings() {
        return settings;
    }

    public static final class Settings {
        private final Set<String> disabledWorlds;
        private final boolean countPlayerPlacedBlocks;
        private final boolean ignorePlayerPlacedBlocksForFullyGrownCrops;
        private final boolean hideAttributes;
        private final boolean preventEnchantedBooksOnLevelToolsItems;
        private final LevelUpSound levelUpSound;

        private Settings(
                @NotNull Set<String> disabledWorlds,
                boolean countPlayerPlacedBlocks,
                boolean ignorePlayerPlacedBlocksForFullyGrownCrops,
                boolean hideAttributes,
                boolean preventEnchantedBooksOnLevelToolsItems,
                @Nullable LevelUpSound levelUpSound
        ) {
            this.disabledWorlds = disabledWorlds;
            this.countPlayerPlacedBlocks = countPlayerPlacedBlocks;
            this.ignorePlayerPlacedBlocksForFullyGrownCrops = ignorePlayerPlacedBlocksForFullyGrownCrops;
            this.hideAttributes = hideAttributes;
            this.preventEnchantedBooksOnLevelToolsItems = preventEnchantedBooksOnLevelToolsItems;
            this.levelUpSound = levelUpSound;
        }

        @NotNull
        static Settings from(@NotNull FileConfiguration config, @NotNull Logger logger) {
            return new Settings(
                    Collections.unmodifiableSet(new HashSet<>(config.getStringList("disabled_worlds"))),
                    config.getBoolean("playerPlacedBlocks"),
                    config.getBoolean("farming.ignore_player_placed_blocks_for_fully_grown_crops", true),
                    config.getBoolean("hide_attributes", true),
                    config.getBoolean("prevent_enchanted_books_on_leveltools_items", false),
                    LevelUpSound.from(config.getConfigurationSection("level_up_sound"), logger)
            );
        }

        @NotNull
        public Set<String> getDisabledWorlds() {
            return disabledWorlds;
        }

        public boolean isCountPlayerPlacedBlocks() {
            return countPlayerPlacedBlocks;
        }

        public boolean isIgnorePlayerPlacedBlocksForFullyGrownCrops() {
            return ignorePlayerPlacedBlocksForFullyGrownCrops;
        }

        public boolean isHideAttributes() {
            return hideAttributes;
        }

        public boolean isPreventEnchantedBooksOnLevelToolsItems() {
            return preventEnchantedBooksOnLevelToolsItems;
        }

        @Nullable
        public LevelUpSound getLevelUpSound() {
            return levelUpSound;
        }
    }

    public static final class LevelUpSound {
        private final Sound sound;
        private final float pitch;
        private final float volume;

        private LevelUpSound(@NotNull Sound sound, float pitch, float volume) {
            this.sound = sound;
            this.pitch = pitch;
            this.volume = volume;
        }

        @Nullable
        static LevelUpSound from(@Nullable ConfigurationSection section, @NotNull Logger logger) {
            if (section == null) {
                return null;
            }

            String name = section.getString("sound", null);
            if (name == null) {
                return null;
            }

            XSound parsed = XSound.matchXSound(name).orElse(null);
            Sound sound = parsed != null && parsed.isSupported() ? parsed.parseSound() : null;
            if (sound == null) {
                logger.warning("The level_up_sound '" + name + "' is unknown or unsupported on this server version. Please fix your configuration.");
                return null;
            }

            return new LevelUpSound(sound, (float) section.getDouble("pitch"), (float) section.getDouble("volume"));
        }

        public void play(@NotNull Player player) {
            player.playSound(player.getLocation(), sound, pitch, volume);
        }
    }
}
