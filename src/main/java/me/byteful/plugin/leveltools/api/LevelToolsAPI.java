package me.byteful.plugin.leveltools.api;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerRegistry;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.reward.RewardProfile;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public final class LevelToolsAPI {

    private LevelToolsAPI() {}

    @NotNull
    public static LevelToolsPlugin getPlugin() {
        LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
        if (instance == null) {
            throw new IllegalStateException("LevelTools is not enabled");
        }
        return instance;
    }

    public static boolean isEnabled() {
        return LevelToolsPlugin.getInstance() != null;
    }

    public static void registerTrigger(@NotNull Trigger trigger) {
        getTriggerRegistry().register(trigger);
    }

    public static boolean unregisterTrigger(@NotNull String triggerId) {
        return getTriggerRegistry().unregister(triggerId);
    }

    @Nullable
    public static Trigger getTrigger(@NotNull String triggerId) {
        return getTriggerRegistry().get(triggerId);
    }

    @NotNull
    public static Optional<Trigger> findTrigger(@NotNull String triggerId) {
        return getTriggerRegistry().getOptional(triggerId);
    }

    public static boolean hasTrigger(@NotNull String triggerId) {
        return getTriggerRegistry().has(triggerId);
    }

    @NotNull
    public static Set<String> getRegisteredTriggerIds() {
        return getTriggerRegistry().getRegisteredIds();
    }

    @NotNull
    public static Collection<Trigger> getAllTriggers() {
        return getTriggerRegistry().getAll();
    }

    @NotNull
    public static RegistrationResult registerTriggerProfile(@NotNull TriggerProfile profile) {
        return getProfileManager().registerTriggerProfile(profile);
    }

    @NotNull
    public static RegistrationResult registerRewardProfile(@NotNull RewardProfile profile) {
        return getProfileManager().registerRewardProfile(profile);
    }

    @NotNull
    public static RegistrationResult registerDisplayProfile(@NotNull DisplayProfile profile) {
        return getProfileManager().registerDisplayProfile(profile);
    }

    @NotNull
    public static RegistrationResult registerItemProfile(@NotNull ItemProfile profile) {
        return getProfileManager().registerItemProfile(profile);
    }

    public static boolean unregisterTriggerProfile(@NotNull String id) {
        return getProfileManager().unregisterTriggerProfile(id);
    }

    public static boolean unregisterRewardProfile(@NotNull String id) {
        return getProfileManager().unregisterRewardProfile(id);
    }

    public static boolean unregisterDisplayProfile(@NotNull String id) {
        return getProfileManager().unregisterDisplayProfile(id);
    }

    public static boolean unregisterItemProfile(@NotNull String id) {
        return getProfileManager().unregisterItemProfile(id);
    }

    @Nullable
    public static TriggerProfile getTriggerProfile(@NotNull String id) {
        return getProfileManager().getTriggerProfile(id);
    }

    @Nullable
    public static RewardProfile getRewardProfile(@NotNull String id) {
        return getProfileManager().getRewardProfile(id);
    }

    @Nullable
    public static DisplayProfile getDisplayProfile(@NotNull String id) {
        return getProfileManager().getDisplayProfile(id);
    }

    @Nullable
    public static ItemProfile getItemProfile(@NotNull String id) {
        return getProfileManager().getItemProfile(id);
    }

    @Nullable
    public static ItemProfile getItemProfileForMaterial(@NotNull Material material) {
        return getProfileManager().getProfileForMaterial(material);
    }

    @NotNull
    public static Collection<TriggerProfile> getAllTriggerProfiles() {
        return getProfileManager().getTriggerProfiles();
    }

    @NotNull
    public static Collection<RewardProfile> getAllRewardProfiles() {
        return getProfileManager().getRewardProfiles();
    }

    @NotNull
    public static Collection<DisplayProfile> getAllDisplayProfiles() {
        return getProfileManager().getDisplayProfiles();
    }

    @NotNull
    public static Collection<ItemProfile> getAllItemProfiles() {
        return getProfileManager().getItemProfiles();
    }

    @NotNull
    public static Set<String> getTriggerProfileIds() {
        return getProfileManager().getTriggerProfileIds();
    }

    @NotNull
    public static Set<String> getRewardProfileIds() {
        return getProfileManager().getRewardProfileIds();
    }

    @NotNull
    public static Set<String> getDisplayProfileIds() {
        return getProfileManager().getDisplayProfileIds();
    }

    @NotNull
    public static Set<String> getItemProfileIds() {
        return getProfileManager().getItemProfileIds();
    }

    @NotNull
    private static TriggerRegistry getTriggerRegistry() {
        return getPlugin().getTriggerRegistry();
    }

    @NotNull
    private static ProfileManager getProfileManager() {
        return getPlugin().getProfileManager();
    }
}
