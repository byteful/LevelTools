package me.byteful.plugin.leveltools.profile;

import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.reward.RewardProfile;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Logger;

public final class ProfileValidator {
    private final Logger logger;

    public ProfileValidator(@NotNull Logger logger) {
        this.logger = logger;
    }

    @NotNull
    public ValidationResult validate(
            @NotNull Map<String, TriggerProfile> triggerProfiles,
            @NotNull Map<String, RewardProfile> rewardProfiles,
            @NotNull Map<String, DisplayProfile> displayProfiles,
            @NotNull Map<String, ItemProfile> itemProfiles
    ) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        validateDuplicateMaterials(itemProfiles, errors);
        validateProfileReferences(itemProfiles, triggerProfiles, rewardProfiles, displayProfiles, errors);
        validateEmptyProfiles(itemProfiles, warnings);

        return new ValidationResult(errors, warnings);
    }

    private void validateDuplicateMaterials(
            @NotNull Map<String, ItemProfile> itemProfiles,
            @NotNull List<String> errors
    ) {
        Map<Material, String> materialToProfile = new HashMap<>();

        for (ItemProfile profile : itemProfiles.values()) {
            for (Material material : profile.getMaterials()) {
                String existingProfile = materialToProfile.get(material);
                if (existingProfile != null) {
                    errors.add("Material " + material.name() + " is defined in multiple profiles: '" + existingProfile + "' and '" + profile.getId() + "'");
                } else {
                    materialToProfile.put(material, profile.getId());
                }
            }
        }
    }

    private void validateProfileReferences(
            @NotNull Map<String, ItemProfile> itemProfiles,
            @NotNull Map<String, TriggerProfile> triggerProfiles,
            @NotNull Map<String, RewardProfile> rewardProfiles,
            @NotNull Map<String, DisplayProfile> displayProfiles,
            @NotNull List<String> errors
    ) {
        for (ItemProfile itemProfile : itemProfiles.values()) {
            String triggerId = itemProfile.getTriggerProfileId();
            if (!triggerProfiles.containsKey(triggerId)) {
                errors.add("Item profile '" + itemProfile.getId() + "' references unknown trigger profile: '" + triggerId + "'");
            }

            String rewardId = itemProfile.getRewardProfileId();
            if (!rewardProfiles.containsKey(rewardId)) {
                errors.add("Item profile '" + itemProfile.getId() + "' references unknown reward profile: '" + rewardId + "'");
            }

            String displayId = itemProfile.getDisplayProfileId();
            if (!displayProfiles.containsKey(displayId)) {
                errors.add("Item profile '" + itemProfile.getId() + "' references unknown display profile: '" + displayId + "'");
            }
        }
    }

    private void validateEmptyProfiles(
            @NotNull Map<String, ItemProfile> itemProfiles,
            @NotNull List<String> warnings
    ) {
        for (ItemProfile profile : itemProfiles.values()) {
            if (profile.getMaterials().isEmpty()) {
                warnings.add("Item profile '" + profile.getId() + "' has no materials defined");
            }
        }
    }

    public static final class ValidationResult {
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult(@NotNull List<String> errors, @NotNull List<String> warnings) {
            this.errors = Collections.unmodifiableList(errors);
            this.warnings = Collections.unmodifiableList(warnings);
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        @NotNull
        public List<String> getErrors() {
            return errors;
        }

        @NotNull
        public List<String> getWarnings() {
            return warnings;
        }

        public void logTo(@NotNull Logger logger) {
            for (String warning : warnings) {
                logger.warning("[Profile Validation] " + warning);
            }
            for (String error : errors) {
                logger.severe("[Profile Validation] " + error);
            }
        }
    }
}
