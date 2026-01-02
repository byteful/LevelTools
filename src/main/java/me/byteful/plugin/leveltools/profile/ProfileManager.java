package me.byteful.plugin.leveltools.profile;

import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.display.DisplayProfileLoader;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.item.ItemProfileLoader;
import me.byteful.plugin.leveltools.profile.reward.RewardProfile;
import me.byteful.plugin.leveltools.profile.reward.RewardProfileLoader;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfileLoader;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

public final class ProfileManager {
    private final Logger logger;
    private final ProfileValidator validator;
    private final TriggerProfileLoader triggerLoader;
    private final RewardProfileLoader rewardLoader;
    private final DisplayProfileLoader displayLoader;
    private final ItemProfileLoader itemLoader;

    // Volatile references for thread-safe atomic swapping during reload (Folia compatibility)
    private volatile Map<String, TriggerProfile> triggerProfiles = Collections.emptyMap();
    private volatile Map<String, RewardProfile> rewardProfiles = Collections.emptyMap();
    private volatile Map<String, DisplayProfile> displayProfiles = Collections.emptyMap();
    private volatile Map<String, ItemProfile> itemProfiles = Collections.emptyMap();
    private volatile Map<Material, ItemProfile> materialToProfile = Collections.emptyMap();

    public ProfileManager(@NotNull Logger logger) {
        this.logger = logger;
        this.validator = new ProfileValidator(logger);
        this.triggerLoader = new TriggerProfileLoader(logger);
        this.rewardLoader = new RewardProfileLoader(logger);
        this.displayLoader = new DisplayProfileLoader(logger);
        this.itemLoader = new ItemProfileLoader(logger);
    }

    public void load(
            @NotNull FileConfiguration triggerConfig,
            @NotNull FileConfiguration rewardConfig,
            @NotNull FileConfiguration displayConfig,
            @NotNull FileConfiguration itemConfig
    ) {
        // Load into new maps (not modifying existing references)
        Map<String, TriggerProfile> newTriggerProfiles = triggerLoader.load(triggerConfig);
        Map<String, RewardProfile> newRewardProfiles = rewardLoader.load(rewardConfig);
        Map<String, DisplayProfile> newDisplayProfiles = displayLoader.load(displayConfig);
        Map<String, ItemProfile> newItemProfiles = itemLoader.load(itemConfig);

        ProfileValidator.ValidationResult result = validator.validate(
                newTriggerProfiles, newRewardProfiles, newDisplayProfiles, newItemProfiles
        );

        result.logTo(logger);

        if (result.hasErrors()) {
            throw new IllegalStateException("Profile validation failed with " + result.getErrors().size() + " error(s). Check the logs above.");
        }

        Map<Material, ItemProfile> newMaterialToProfile = buildMaterialMapping(newItemProfiles);

        // Atomically swap all references (thread-safe for Folia)
        this.triggerProfiles = Collections.unmodifiableMap(newTriggerProfiles);
        this.rewardProfiles = Collections.unmodifiableMap(newRewardProfiles);
        this.displayProfiles = Collections.unmodifiableMap(newDisplayProfiles);
        this.itemProfiles = Collections.unmodifiableMap(newItemProfiles);
        this.materialToProfile = Collections.unmodifiableMap(newMaterialToProfile);

        logger.info("Profile system initialized successfully.");
    }

    @NotNull
    private Map<Material, ItemProfile> buildMaterialMapping(@NotNull Map<String, ItemProfile> items) {
        Map<Material, ItemProfile> mapping = new HashMap<>();

        for (ItemProfile profile : items.values()) {
            for (Material material : profile.getMaterials()) {
                mapping.put(material, profile);
            }
        }

        logger.info("Mapped " + mapping.size() + " material(s) to item profiles.");
        return mapping;
    }

    public void clear() {
        this.triggerProfiles = Collections.emptyMap();
        this.rewardProfiles = Collections.emptyMap();
        this.displayProfiles = Collections.emptyMap();
        this.itemProfiles = Collections.emptyMap();
        this.materialToProfile = Collections.emptyMap();
    }

    @Nullable
    public ItemProfile getProfileForMaterial(@NotNull Material material) {
        return materialToProfile.get(material);
    }

    public boolean hasMaterialProfile(@NotNull Material material) {
        return materialToProfile.containsKey(material);
    }

    @Nullable
    public TriggerProfile getTriggerProfile(@NotNull String id) {
        return triggerProfiles.get(id);
    }

    @Nullable
    public RewardProfile getRewardProfile(@NotNull String id) {
        return rewardProfiles.get(id);
    }

    @Nullable
    public DisplayProfile getDisplayProfile(@NotNull String id) {
        return displayProfiles.get(id);
    }

    @Nullable
    public ItemProfile getItemProfile(@NotNull String id) {
        return itemProfiles.get(id);
    }

    @Nullable
    public TriggerProfile getTriggerProfileFor(@NotNull ItemProfile itemProfile) {
        return triggerProfiles.get(itemProfile.getTriggerProfileId());
    }

    @Nullable
    public RewardProfile getRewardProfileFor(@NotNull ItemProfile itemProfile) {
        return rewardProfiles.get(itemProfile.getRewardProfileId());
    }

    @Nullable
    public DisplayProfile getDisplayProfileFor(@NotNull ItemProfile itemProfile) {
        return displayProfiles.get(itemProfile.getDisplayProfileId());
    }

    @NotNull
    public Collection<TriggerProfile> getTriggerProfiles() {
        return Collections.unmodifiableCollection(triggerProfiles.values());
    }

    @NotNull
    public Collection<RewardProfile> getRewardProfiles() {
        return Collections.unmodifiableCollection(rewardProfiles.values());
    }

    @NotNull
    public Collection<DisplayProfile> getDisplayProfiles() {
        return Collections.unmodifiableCollection(displayProfiles.values());
    }

    @NotNull
    public Collection<ItemProfile> getItemProfiles() {
        return Collections.unmodifiableCollection(itemProfiles.values());
    }

    @NotNull
    public Set<String> getTriggerProfileIds() {
        return Collections.unmodifiableSet(triggerProfiles.keySet());
    }

    @NotNull
    public Set<String> getRewardProfileIds() {
        return Collections.unmodifiableSet(rewardProfiles.keySet());
    }

    @NotNull
    public Set<String> getDisplayProfileIds() {
        return Collections.unmodifiableSet(displayProfiles.keySet());
    }

    @NotNull
    public Set<String> getItemProfileIds() {
        return Collections.unmodifiableSet(itemProfiles.keySet());
    }
}
