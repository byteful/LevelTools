package me.byteful.plugin.leveltools.profile;

import me.byteful.plugin.leveltools.api.ProfileType;
import me.byteful.plugin.leveltools.api.RegistrationResult;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class ProfileManager {
    private final Logger logger;
    private final ProfileValidator validator;
    private final TriggerProfileLoader triggerLoader;
    private final RewardProfileLoader rewardLoader;
    private final DisplayProfileLoader displayLoader;
    private final ItemProfileLoader itemLoader;

    private volatile Map<String, TriggerProfile> triggerProfiles = new ConcurrentHashMap<>();
    private volatile Map<String, RewardProfile> rewardProfiles = new ConcurrentHashMap<>();
    private volatile Map<String, DisplayProfile> displayProfiles = new ConcurrentHashMap<>();
    private volatile Map<String, ItemProfile> itemProfiles = new ConcurrentHashMap<>();
    private volatile Map<Material, ItemProfile> materialToProfile = new ConcurrentHashMap<>();

    private final Set<String> externalTriggerProfiles = ConcurrentHashMap.newKeySet();
    private final Set<String> externalRewardProfiles = ConcurrentHashMap.newKeySet();
    private final Set<String> externalDisplayProfiles = ConcurrentHashMap.newKeySet();
    private final Set<String> externalItemProfiles = ConcurrentHashMap.newKeySet();

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
        Map<String, TriggerProfile> savedExtTrigger = new HashMap<>();
        Map<String, RewardProfile> savedExtReward = new HashMap<>();
        Map<String, DisplayProfile> savedExtDisplay = new HashMap<>();
        Map<String, ItemProfile> savedExtItem = new HashMap<>();

        for (String id : externalTriggerProfiles) {
            TriggerProfile profile = triggerProfiles.get(id);
            if (profile != null) savedExtTrigger.put(id, profile);
        }
        for (String id : externalRewardProfiles) {
            RewardProfile profile = rewardProfiles.get(id);
            if (profile != null) savedExtReward.put(id, profile);
        }
        for (String id : externalDisplayProfiles) {
            DisplayProfile profile = displayProfiles.get(id);
            if (profile != null) savedExtDisplay.put(id, profile);
        }
        for (String id : externalItemProfiles) {
            ItemProfile profile = itemProfiles.get(id);
            if (profile != null) savedExtItem.put(id, profile);
        }

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

        newTriggerProfiles.putAll(savedExtTrigger);
        newRewardProfiles.putAll(savedExtReward);
        newDisplayProfiles.putAll(savedExtDisplay);
        newItemProfiles.putAll(savedExtItem);

        Map<Material, ItemProfile> newMaterialToProfile = buildMaterialMapping(newItemProfiles);

        this.triggerProfiles = new ConcurrentHashMap<>(newTriggerProfiles);
        this.rewardProfiles = new ConcurrentHashMap<>(newRewardProfiles);
        this.displayProfiles = new ConcurrentHashMap<>(newDisplayProfiles);
        this.itemProfiles = new ConcurrentHashMap<>(newItemProfiles);
        this.materialToProfile = new ConcurrentHashMap<>(newMaterialToProfile);

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
        this.triggerProfiles = new ConcurrentHashMap<>();
        this.rewardProfiles = new ConcurrentHashMap<>();
        this.displayProfiles = new ConcurrentHashMap<>();
        this.itemProfiles = new ConcurrentHashMap<>();
        this.materialToProfile = new ConcurrentHashMap<>();
        this.externalTriggerProfiles.clear();
        this.externalRewardProfiles.clear();
        this.externalDisplayProfiles.clear();
        this.externalItemProfiles.clear();
    }

    @NotNull
    public RegistrationResult registerTriggerProfile(@NotNull TriggerProfile profile) {
        String id = profile.getId();
        if (triggerProfiles.containsKey(id)) {
            return RegistrationResult.alreadyExists(id, "trigger");
        }
        triggerProfiles.put(id, profile);
        externalTriggerProfiles.add(id);
        logger.info("Registered external trigger profile: " + id);
        return RegistrationResult.success(id, "trigger");
    }

    @NotNull
    public RegistrationResult registerRewardProfile(@NotNull RewardProfile profile) {
        String id = profile.getId();
        if (rewardProfiles.containsKey(id)) {
            return RegistrationResult.alreadyExists(id, "reward");
        }
        rewardProfiles.put(id, profile);
        externalRewardProfiles.add(id);
        logger.info("Registered external reward profile: " + id);
        return RegistrationResult.success(id, "reward");
    }

    @NotNull
    public RegistrationResult registerDisplayProfile(@NotNull DisplayProfile profile) {
        String id = profile.getId();
        if (displayProfiles.containsKey(id)) {
            return RegistrationResult.alreadyExists(id, "display");
        }
        displayProfiles.put(id, profile);
        externalDisplayProfiles.add(id);
        logger.info("Registered external display profile: " + id);
        return RegistrationResult.success(id, "display");
    }

    @NotNull
    public RegistrationResult registerItemProfile(@NotNull ItemProfile profile) {
        String id = profile.getId();
        if (itemProfiles.containsKey(id)) {
            return RegistrationResult.alreadyExists(id, "item");
        }

        if (!triggerProfiles.containsKey(profile.getTriggerProfileId())) {
            return RegistrationResult.missingReference(id, "trigger", profile.getTriggerProfileId());
        }
        if (!rewardProfiles.containsKey(profile.getRewardProfileId())) {
            return RegistrationResult.missingReference(id, "reward", profile.getRewardProfileId());
        }
        if (!displayProfiles.containsKey(profile.getDisplayProfileId())) {
            return RegistrationResult.missingReference(id, "display", profile.getDisplayProfileId());
        }

        for (Material material : profile.getMaterials()) {
            if (materialToProfile.containsKey(material)) {
                return RegistrationResult.materialConflict(id, material, materialToProfile.get(material).getId());
            }
        }

        itemProfiles.put(id, profile);
        for (Material material : profile.getMaterials()) {
            materialToProfile.put(material, profile);
        }
        externalItemProfiles.add(id);
        logger.info("Registered external item profile: " + id);
        return RegistrationResult.success(id, "item");
    }

    public boolean unregisterTriggerProfile(@NotNull String id) {
        if (!externalTriggerProfiles.contains(id)) {
            return false;
        }
        triggerProfiles.remove(id);
        externalTriggerProfiles.remove(id);
        logger.info("Unregistered external trigger profile: " + id);
        return true;
    }

    public boolean unregisterRewardProfile(@NotNull String id) {
        if (!externalRewardProfiles.contains(id)) {
            return false;
        }
        rewardProfiles.remove(id);
        externalRewardProfiles.remove(id);
        logger.info("Unregistered external reward profile: " + id);
        return true;
    }

    public boolean unregisterDisplayProfile(@NotNull String id) {
        if (!externalDisplayProfiles.contains(id)) {
            return false;
        }
        displayProfiles.remove(id);
        externalDisplayProfiles.remove(id);
        logger.info("Unregistered external display profile: " + id);
        return true;
    }

    public boolean unregisterItemProfile(@NotNull String id) {
        if (!externalItemProfiles.contains(id)) {
            return false;
        }
        ItemProfile profile = itemProfiles.remove(id);
        if (profile != null) {
            for (Material material : profile.getMaterials()) {
                materialToProfile.remove(material);
            }
        }
        externalItemProfiles.remove(id);
        logger.info("Unregistered external item profile: " + id);
        return true;
    }

    public boolean isExternalProfile(@NotNull String id, @NotNull ProfileType type) {
        switch (type) {
            case TRIGGER:
                return externalTriggerProfiles.contains(id);
            case REWARD:
                return externalRewardProfiles.contains(id);
            case DISPLAY:
                return externalDisplayProfiles.contains(id);
            case ITEM:
                return externalItemProfiles.contains(id);
            default:
                return false;
        }
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
