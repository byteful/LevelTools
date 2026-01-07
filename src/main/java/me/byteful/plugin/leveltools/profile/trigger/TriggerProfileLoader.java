package me.byteful.plugin.leveltools.profile.trigger;

import me.byteful.plugin.leveltools.api.trigger.TriggerIds;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public final class TriggerProfileLoader {
    private final Logger logger;

    public TriggerProfileLoader(@NotNull Logger logger) {
        this.logger = logger;
    }

    @NotNull
    public Map<String, TriggerProfile> load(@NotNull FileConfiguration config) {
        Map<String, TriggerProfile> profiles = new HashMap<>();
        ConfigurationSection profilesSection = config.getConfigurationSection("profiles");

        if (profilesSection == null) {
            logger.warning("No trigger profiles found in trigger_profiles.yml");
            return profiles;
        }

        for (String profileId : profilesSection.getKeys(false)) {
            ConfigurationSection profileSection = profilesSection.getConfigurationSection(profileId);
            if (profileSection == null) continue;

            try {
                TriggerProfile profile = parseProfile(profileId, profileSection);
                profiles.put(profileId, profile);
            } catch (Exception e) {
                logger.severe("Failed to load trigger profile '" + profileId + "': " + e.getMessage());
            }
        }

        logger.info("Loaded " + profiles.size() + " trigger profile(s).");
        return profiles;
    }

    @NotNull
    private TriggerProfile parseProfile(@NotNull String id, @NotNull ConfigurationSection section) {
        String typeStr = section.getString("type");
        if (typeStr == null) {
            throw new IllegalArgumentException("Missing 'type' field");
        }

        String triggerId = TriggerIds.normalize(typeStr);
        if (triggerId.isEmpty()) {
            throw new IllegalArgumentException("Invalid trigger type: " + typeStr);
        }

        XpModifierConfig xpModifier = parseXpModifier(section.getConfigurationSection("xp_modifier"));
        TriggerFilter filter = parseFilter(section.getConfigurationSection("filter"));
        TriggerSettings settings = parseSettings(section, triggerId);

        return TriggerProfile.builder(id)
                .triggerId(triggerId)
                .xpModifier(xpModifier)
                .filter(filter)
                .settings(settings)
                .build();
    }

    @NotNull
    private XpModifierConfig parseXpModifier(ConfigurationSection section) {
        if (section == null) {
            return new XpModifierConfig(1.0, 1.0, Collections.emptyMap());
        }

        ConfigurationSection defaultSection = section.getConfigurationSection("default");
        double defaultMin = 1.0;
        double defaultMax = 1.0;

        if (defaultSection != null) {
            defaultMin = defaultSection.getDouble("min", 1.0);
            defaultMax = defaultSection.getDouble("max", 1.0);
        }

        Map<String, XpModifierConfig.XpModifierRange> customModifiers = new HashMap<>();
        ConfigurationSection customSection = section.getConfigurationSection("custom");

        if (customSection != null) {
            for (String key : customSection.getKeys(false)) {
                ConfigurationSection modSection = customSection.getConfigurationSection(key);
                if (modSection != null) {
                    double min = modSection.getDouble("min", defaultMin);
                    double max = modSection.getDouble("max", defaultMax);
                    customModifiers.put(key.toUpperCase(), new XpModifierConfig.XpModifierRange(min, max));
                }
            }
        }

        return new XpModifierConfig(defaultMin, defaultMax, customModifiers);
    }

    @NotNull
    private TriggerFilter parseFilter(ConfigurationSection section) {
        if (section == null) {
            return TriggerFilter.empty();
        }

        String typeStr = section.getString("type", "BLACKLIST");
        boolean whitelist = typeStr.equalsIgnoreCase("WHITELIST");

        List<String> list = section.getStringList("list");
        Set<String> entries = new HashSet<>();
        for (String entry : list) {
            entries.add(entry.toUpperCase());
        }

        return new TriggerFilter(whitelist, entries);
    }

    @NotNull
    private TriggerSettings parseSettings(@NotNull ConfigurationSection section, @NotNull String triggerId) {
        if (TriggerIds.RIGHT_CLICK.equals(triggerId) || TriggerIds.LEFT_CLICK.equals(triggerId)) {
            String clickModeStr = section.getString("click_mode", "ANY");
            TriggerSettings.ClickMode clickMode = TriggerSettings.ClickMode.fromString(clickModeStr);
            return TriggerSettings.forClick(clickMode);
        }
        return TriggerSettings.empty();
    }
}
