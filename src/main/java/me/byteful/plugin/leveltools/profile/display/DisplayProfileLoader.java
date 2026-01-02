package me.byteful.plugin.leveltools.profile.display;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class DisplayProfileLoader {
    private final Logger logger;

    public DisplayProfileLoader(@NotNull Logger logger) {
        this.logger = logger;
    }

    @NotNull
    public Map<String, DisplayProfile> load(@NotNull FileConfiguration config) {
        Map<String, DisplayProfile> profiles = new HashMap<>();
        ConfigurationSection profilesSection = config.getConfigurationSection("profiles");

        if (profilesSection == null) {
            logger.warning("No display profiles found in display_profiles.yml");
            return profiles;
        }

        for (String profileId : profilesSection.getKeys(false)) {
            ConfigurationSection profileSection = profilesSection.getConfigurationSection(profileId);
            if (profileSection == null) continue;

            try {
                DisplayProfile profile = parseProfile(profileId, profileSection);
                profiles.put(profileId, profile);
            } catch (Exception e) {
                logger.severe("Failed to load display profile '" + profileId + "': " + e.getMessage());
            }
        }

        logger.info("Loaded " + profiles.size() + " display profile(s).");
        return profiles;
    }

    @NotNull
    private DisplayProfile parseProfile(@NotNull String id, @NotNull ConfigurationSection section) {
        DisplayProfile.NameDisplay nameDisplay = parseNameDisplay(section.getConfigurationSection("name"));
        DisplayProfile.ActionBarDisplay actionBarDisplay = parseActionBarDisplay(section.getConfigurationSection("action_bar"));
        DisplayProfile.LoreDisplay loreDisplay = parseLoreDisplay(section.getConfigurationSection("lore"));
        ProgressBarConfig progressBar = parseProgressBar(section.getConfigurationSection("progress_bar"));

        return DisplayProfile.builder(id)
                .nameDisplay(nameDisplay)
                .actionBarDisplay(actionBarDisplay)
                .loreDisplay(loreDisplay)
                .progressBar(progressBar)
                .build();
    }

    @NotNull
    private DisplayProfile.NameDisplay parseNameDisplay(ConfigurationSection section) {
        if (section == null) {
            return DisplayProfile.NameDisplay.disabled();
        }

        boolean enabled = section.getBoolean("enabled", false);
        String text = section.getString("text", "{item} &7- &b{level}");

        return new DisplayProfile.NameDisplay(enabled, text);
    }

    @NotNull
    private DisplayProfile.ActionBarDisplay parseActionBarDisplay(ConfigurationSection section) {
        if (section == null) {
            return DisplayProfile.ActionBarDisplay.disabled();
        }

        boolean enabled = section.getBoolean("enabled", true);
        String text = section.getString("text", "{progress_bar} &e{xp_formatted}&6/&e{max_xp_formatted}");

        return new DisplayProfile.ActionBarDisplay(enabled, text);
    }

    @NotNull
    private DisplayProfile.LoreDisplay parseLoreDisplay(ConfigurationSection section) {
        if (section == null) {
            return DisplayProfile.LoreDisplay.disabled();
        }

        boolean enabled = section.getBoolean("enabled", true);
        List<String> lines = section.getStringList("lines");

        return new DisplayProfile.LoreDisplay(enabled, lines);
    }

    @NotNull
    private ProgressBarConfig parseProgressBar(ConfigurationSection section) {
        if (section == null) {
            return ProgressBarConfig.defaultConfig();
        }

        return ProgressBarConfig.builder()
                .totalBars(section.getInt("total_bars", 50))
                .barSymbol(getChar(section, "bar_symbol", '|'))
                .prefixSymbol(getChar(section, "prefix_symbol", '['))
                .suffixSymbol(getChar(section, "suffix_symbol", ']'))
                .prefixColor(getChar(section, "prefix_color", '8'))
                .suffixColor(getChar(section, "suffix_color", '8'))
                .completedColor(getChar(section, "completed_color", 'e'))
                .placeholderColor(getChar(section, "placeholder_color", '7'))
                .build();
    }

    private char getChar(@NotNull ConfigurationSection section, @NotNull String key, char defaultValue) {
        String value = section.getString(key);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value.charAt(0);
    }
}
