package me.byteful.plugin.leveltools.config.migration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public final class V1ToV2Migration {
    private final YamlConfiguration oldConfig;
    private final Path dataFolder;
    private final Logger logger;

    public V1ToV2Migration(@NotNull YamlConfiguration oldConfig, @NotNull Path dataFolder, @NotNull Logger logger) {
        this.oldConfig = oldConfig;
        this.dataFolder = dataFolder;
        this.logger = logger;
    }

    public void execute() {
        createNewMainConfig();
        createTriggerProfiles();
        createRewardProfiles();
        createDisplayProfiles();
        createItemProfiles();
        logger.info("v1 to v2 migration completed successfully.");
    }

    private void createNewMainConfig() {
        YamlConfiguration newConfig = new YamlConfiguration();

        newConfig.set("update.start", oldConfig.getBoolean("update.start", true));
        newConfig.set("update.periodically", oldConfig.getBoolean("update.periodically", true));

        newConfig.set("block_data_storage.type", oldConfig.getString("block_data_storage.type", "SQLITE"));

        copySection(oldConfig.getConfigurationSection("messages"), newConfig, "messages");

        newConfig.set("anvil_combine", oldConfig.getString("anvil_combine", "ADD_BOTH"));
        newConfig.set("level_xp_formula", oldConfig.getString("level_xp_formula", "100 + {current_level} * 100"));
        newConfig.set("playerPlacedBlocks", oldConfig.getBoolean("playerPlacedBlocks", false));

        copySection(oldConfig.getConfigurationSection("level_up_sound"), newConfig, "level_up_sound");

        newConfig.set("hide_attributes", oldConfig.getBoolean("hide_attributes", true));
        newConfig.set("disabled_worlds", oldConfig.getStringList("disabled_worlds"));

        saveConfig(newConfig, "config.yml", CONFIG_HEADER);
    }

    private void createTriggerProfiles() {
        YamlConfiguration triggerConfig = new YamlConfiguration();

        triggerConfig.set("profiles.block_mining.type", "BLOCK_BREAK");
        copyXpModifier(oldConfig, triggerConfig, "default_block_xp_modifier", "block_xp_modifiers", "profiles.block_mining.xp_modifier");
        copyFilter(oldConfig, triggerConfig, "block_list_type", "block_list", "profiles.block_mining.filter");

        triggerConfig.set("profiles.combat.type", "ENTITY_KILL");
        copyXpModifier(oldConfig, triggerConfig, "default_combat_xp_modifier", "combat_xp_modifiers", "profiles.combat.xp_modifier");
        copyFilter(oldConfig, triggerConfig, "entity_list_type", "entity_list", "profiles.combat.filter");

        triggerConfig.set("profiles.fishing.type", "FISHING");
        triggerConfig.set("profiles.fishing.xp_modifier.default.min", 1.0);
        triggerConfig.set("profiles.fishing.xp_modifier.default.max", 5.0);
        triggerConfig.set("profiles.fishing.filter.type", "BLACKLIST");
        triggerConfig.set("profiles.fishing.filter.list", Collections.emptyList());

        saveConfig(triggerConfig, "trigger_profiles.yml", TRIGGER_HEADER);
    }

    private void copyXpModifier(@NotNull YamlConfiguration source, @NotNull YamlConfiguration target,
                                @NotNull String defaultKey, @NotNull String customKey, @NotNull String targetPath) {
        ConfigurationSection defaultSection = source.getConfigurationSection(defaultKey);
        if (defaultSection != null) {
            target.set(targetPath + ".default.min", defaultSection.getDouble("min", 1.0));
            target.set(targetPath + ".default.max", defaultSection.getDouble("max", 1.0));
        } else {
            target.set(targetPath + ".default.min", 1.0);
            target.set(targetPath + ".default.max", 1.0);
        }

        ConfigurationSection customSection = source.getConfigurationSection(customKey);
        if (customSection != null) {
            for (String key : customSection.getKeys(false)) {
                ConfigurationSection modSection = customSection.getConfigurationSection(key);
                if (modSection != null) {
                    target.set(targetPath + ".custom." + key + ".min", modSection.getDouble("min"));
                    target.set(targetPath + ".custom." + key + ".max", modSection.getDouble("max"));
                }
            }
        }
    }

    private void copyFilter(@NotNull YamlConfiguration source, @NotNull YamlConfiguration target,
                            @NotNull String typeKey, @NotNull String listKey, @NotNull String targetPath) {
        String filterType = source.getString(typeKey, "BLACKLIST");
        List<String> filterList = source.getStringList(listKey);

        target.set(targetPath + ".type", filterType.toUpperCase());
        target.set(targetPath + ".list", filterList);
    }

    private void createRewardProfiles() {
        YamlConfiguration rewardConfig = new YamlConfiguration();

        migrateRewardSection(oldConfig, rewardConfig, "tool_rewards", "tools");
        migrateRewardSection(oldConfig, rewardConfig, "sword_rewards", "swords");
        migrateRewardSection(oldConfig, rewardConfig, "bow_rewards", "bows");

        rewardConfig.set("profiles.fishing_rods.levels.1", Collections.singletonList("enchant2 lure 1"));
        rewardConfig.set("profiles.fishing_rods.levels.5", Collections.singletonList("enchant2 luck_of_the_sea 1"));
        rewardConfig.set("profiles.fishing_rods.levels.10", Collections.singletonList("enchant2 lure 2"));
        rewardConfig.set("profiles.fishing_rods.levels.15", Collections.singletonList("enchant2 unbreaking 1"));
        rewardConfig.set("profiles.fishing_rods.levels.20", Collections.singletonList("enchant2 luck_of_the_sea 2"));
        rewardConfig.set("profiles.fishing_rods.levels.25", Collections.singletonList("enchant2 lure 3"));
        rewardConfig.set("profiles.fishing_rods.levels.30", Collections.singletonList("enchant2 unbreaking 2"));
        rewardConfig.set("profiles.fishing_rods.levels.35", Collections.singletonList("enchant2 luck_of_the_sea 3"));
        rewardConfig.set("profiles.fishing_rods.levels.40", Collections.singletonList("enchant2 unbreaking 3"));
        rewardConfig.set("profiles.fishing_rods.levels.50", Collections.singletonList("enchant2 mending 1"));

        saveConfig(rewardConfig, "reward_profiles.yml", REWARD_HEADER);
    }

    private void migrateRewardSection(@NotNull YamlConfiguration source, @NotNull YamlConfiguration target,
                                      @NotNull String sourceKey, @NotNull String profileId) {
        ConfigurationSection rewardsSection = source.getConfigurationSection(sourceKey);
        if (rewardsSection == null) {
            return;
        }

        for (String levelKey : rewardsSection.getKeys(false)) {
            List<String> rewards = rewardsSection.getStringList(levelKey);
            target.set("profiles." + profileId + ".levels." + levelKey, rewards);
        }
    }

    private void createDisplayProfiles() {
        YamlConfiguration displayConfig = new YamlConfiguration();

        ConfigurationSection displaySection = oldConfig.getConfigurationSection("display");
        ConfigurationSection progressBar = oldConfig.getConfigurationSection("progress_bar");

        String basePath = "profiles.default";

        if (displaySection != null) {
            ConfigurationSection nameSection = displaySection.getConfigurationSection("name");
            if (nameSection != null) {
                displayConfig.set(basePath + ".name.enabled", nameSection.getBoolean("enabled", false));
                displayConfig.set(basePath + ".name.text", nameSection.getString("text", "{item} &7- &b{level}"));
            }

            ConfigurationSection actionBarSection = displaySection.getConfigurationSection("actionBar");
            if (actionBarSection != null) {
                displayConfig.set(basePath + ".action_bar.enabled", actionBarSection.getBoolean("enabled", true));
                displayConfig.set(basePath + ".action_bar.text", actionBarSection.getString("text", "{progress_bar} &e{xp_formatted}&6/&e{max_xp_formatted}"));
            }

            ConfigurationSection loreSection = displaySection.getConfigurationSection("lore");
            if (loreSection != null) {
                displayConfig.set(basePath + ".lore.enabled", loreSection.getBoolean("enabled", true));
                displayConfig.set(basePath + ".lore.lines", loreSection.getStringList("lines"));
            }
        }

        if (progressBar != null) {
            displayConfig.set(basePath + ".progress_bar.total_bars", progressBar.getInt("total_bars", 50));
            displayConfig.set(basePath + ".progress_bar.bar_symbol", progressBar.getString("bar_symbol", "|"));
            displayConfig.set(basePath + ".progress_bar.prefix_symbol", progressBar.getString("prefix_symbol", "["));
            displayConfig.set(basePath + ".progress_bar.suffix_symbol", progressBar.getString("suffix_symbol", "]"));
            displayConfig.set(basePath + ".progress_bar.prefix_color", progressBar.getString("prefix_color", "8"));
            displayConfig.set(basePath + ".progress_bar.suffix_color", progressBar.getString("suffix_color", "8"));
            displayConfig.set(basePath + ".progress_bar.completed_color", progressBar.getString("completed_color", "e"));
            displayConfig.set(basePath + ".progress_bar.placeholder_color", progressBar.getString("placeholder_color", "7"));
        }

        saveConfig(displayConfig, "display_profiles.yml", DISPLAY_HEADER);
    }

    private void createItemProfiles() {
        YamlConfiguration itemConfig = new YamlConfiguration();
        int maxLevel = oldConfig.getInt("max_level", 100);

        createToolProfile(itemConfig, "pickaxes",
                Arrays.asList("WOODEN_PICKAXE", "STONE_PICKAXE", "IRON_PICKAXE", "GOLDEN_PICKAXE", "DIAMOND_PICKAXE", "NETHERITE_PICKAXE"),
                "block_mining", "tools", "default", maxLevel);

        createToolProfile(itemConfig, "axes",
                Arrays.asList("WOODEN_AXE", "STONE_AXE", "IRON_AXE", "GOLDEN_AXE", "DIAMOND_AXE", "NETHERITE_AXE"),
                "block_mining", "tools", "default", maxLevel);

        createToolProfile(itemConfig, "shovels",
                Arrays.asList("WOODEN_SHOVEL", "STONE_SHOVEL", "IRON_SHOVEL", "GOLDEN_SHOVEL", "DIAMOND_SHOVEL", "NETHERITE_SHOVEL"),
                "block_mining", "tools", "default", maxLevel);

        createToolProfile(itemConfig, "swords",
                Arrays.asList("WOODEN_SWORD", "STONE_SWORD", "IRON_SWORD", "GOLDEN_SWORD", "DIAMOND_SWORD", "NETHERITE_SWORD"),
                "combat", "swords", "default", maxLevel);

        createToolProfile(itemConfig, "bows",
                Arrays.asList("BOW", "CROSSBOW"),
                "combat", "bows", "default", maxLevel);

        createToolProfile(itemConfig, "fishing_rods",
                Collections.singletonList("FISHING_ROD"),
                "fishing", "fishing_rods", "default", 50);

        saveConfig(itemConfig, "item_profiles.yml", ITEM_HEADER);
    }

    private void createToolProfile(@NotNull YamlConfiguration config, @NotNull String id,
                                   @NotNull List<String> materials, @NotNull String triggerProfile,
                                   @NotNull String rewardProfile, @NotNull String displayProfile, int maxLevel) {
        String basePath = "profiles." + id;
        config.set(basePath + ".materials", materials);
        config.set(basePath + ".trigger_profile", triggerProfile);
        config.set(basePath + ".reward_profile", rewardProfile);
        config.set(basePath + ".display_profile", displayProfile);
        config.set(basePath + ".max_level", maxLevel);
    }

    private void copySection(ConfigurationSection source, @NotNull YamlConfiguration target, @NotNull String path) {
        if (source == null) {
            return;
        }

        for (String key : source.getKeys(true)) {
            Object value = source.get(key);
            if (!(value instanceof ConfigurationSection)) {
                target.set(path + "." + key, value);
            }
        }
    }

    private void saveConfig(@NotNull YamlConfiguration config, @NotNull String filename, @NotNull String header) {
        try {
            String yamlContent = config.saveToString();
            String fullContent = header + yamlContent;
            Files.write(dataFolder.resolve(filename), fullContent.getBytes(StandardCharsets.UTF_8));
            logger.info("Created " + filename);
        } catch (IOException e) {
            logger.severe("Failed to save " + filename + ": " + e.getMessage());
        }
    }

    private static final String CONFIG_HEADER =
            "#\n" +
            "# LevelTools by byteful\n" +
            "#\n" +
            "# Wiki: https://github.com/byteful/LevelTools/wiki/Configuration\n" +
            "#\n\n";

    private static final String TRIGGER_HEADER =
            "#\n" +
            "# Trigger Profiles - Define how XP is gained\n" +
            "#\n" +
            "# Wiki: https://github.com/byteful/LevelTools/wiki/Trigger-Profiles\n" +
            "#\n\n";

    private static final String REWARD_HEADER =
            "#\n" +
            "# Reward Profiles - Define rewards given at each level\n" +
            "#\n" +
            "# Wiki: https://github.com/byteful/LevelTools/wiki/Reward-Profiles\n" +
            "#\n\n";

    private static final String DISPLAY_HEADER =
            "#\n" +
            "# Display Profiles - Define how leveled items look\n" +
            "#\n" +
            "# Wiki: https://github.com/byteful/LevelTools/wiki/Display-Profiles\n" +
            "#\n\n";

    private static final String ITEM_HEADER =
            "#\n" +
            "# Item Profiles - Define which items can level up\n" +
            "#\n" +
            "# Wiki: https://github.com/byteful/LevelTools/wiki/Item-Profiles\n" +
            "#\n\n";
}
