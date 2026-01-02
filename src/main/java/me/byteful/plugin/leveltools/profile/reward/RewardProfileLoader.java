package me.byteful.plugin.leveltools.profile.reward;

import me.byteful.plugin.leveltools.api.RewardType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public final class RewardProfileLoader {
    private final Logger logger;

    public RewardProfileLoader(@NotNull Logger logger) {
        this.logger = logger;
    }

    @NotNull
    public Map<String, RewardProfile> load(@NotNull FileConfiguration config) {
        Map<String, RewardProfile> profiles = new HashMap<>();
        ConfigurationSection profilesSection = config.getConfigurationSection("profiles");

        if (profilesSection == null) {
            logger.warning("No reward profiles found in reward_profiles.yml");
            return profiles;
        }

        for (String profileId : profilesSection.getKeys(false)) {
            ConfigurationSection profileSection = profilesSection.getConfigurationSection(profileId);
            if (profileSection == null) continue;

            try {
                RewardProfile profile = parseProfile(profileId, profileSection);
                profiles.put(profileId, profile);
            } catch (Exception e) {
                logger.severe("Failed to load reward profile '" + profileId + "': " + e.getMessage());
            }
        }

        logger.info("Loaded " + profiles.size() + " reward profile(s).");
        return profiles;
    }

    @NotNull
    private RewardProfile parseProfile(@NotNull String id, @NotNull ConfigurationSection section) {
        ConfigurationSection levelsSection = section.getConfigurationSection("levels");
        Map<Integer, List<RewardEntry>> levelRewards = new HashMap<>();

        if (levelsSection != null) {
            for (String levelKey : levelsSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(levelKey);
                    List<String> rewardStrings = levelsSection.getStringList(levelKey);
                    List<RewardEntry> entries = parseRewardEntries(rewardStrings);
                    if (!entries.isEmpty()) {
                        levelRewards.put(level, entries);
                    }
                } catch (NumberFormatException e) {
                    logger.warning("Invalid level key '" + levelKey + "' in reward profile '" + id + "'");
                }
            }
        }

        return new RewardProfile(id, levelRewards);
    }

    @NotNull
    private List<RewardEntry> parseRewardEntries(@NotNull List<String> rewardStrings) {
        List<RewardEntry> entries = new ArrayList<>();

        for (String rewardString : rewardStrings) {
            RewardEntry entry = parseRewardEntry(rewardString);
            if (entry != null) {
                entries.add(entry);
            }
        }

        return entries;
    }

    private RewardEntry parseRewardEntry(@NotNull String rewardString) {
        if (rewardString.isEmpty()) {
            return null;
        }

        String[] split = rewardString.split(" ");
        if (split.length < 1) {
            return null;
        }

        String typeKey = split[0].toLowerCase()
                .trim()
                .replace(" ", "-")
                .replace("_", "-");

        Optional<RewardType> typeOpt = RewardType.fromConfigKey(typeKey);
        if (!typeOpt.isPresent()) {
            logger.warning("Unknown reward type: " + split[0]);
            return null;
        }

        return new RewardEntry(typeOpt.get(), split, rewardString);
    }
}
