package me.byteful.plugin.leveltools.util;

import com.cryptomorin.xseries.messages.ActionBar;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.item.impl.NBTLevelToolsItem;
import me.byteful.plugin.leveltools.api.item.impl.PDCLevelToolsItem;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import me.byteful.plugin.leveltools.api.scheduler.impl.bukkit.BukkitScheduler;
import me.byteful.plugin.leveltools.api.scheduler.impl.folia.FoliaScheduler;
import me.byteful.plugin.leveltools.api.trigger.TriggerSlot;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.display.ProgressBarConfig;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.reward.RewardEntry;
import me.byteful.plugin.leveltools.profile.reward.RewardProfile;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.byteful.plugin.leveltools.util.Text.*;

public final class LevelToolsUtil {
    public static final int MID_VERSION;
    private static final Pattern MINECRAFT_VERSION_PATTERN =
            Pattern.compile("^(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:[-+].*|\\.(?!\\d).*)?$");
    private static final Pattern SERVER_MINECRAFT_VERSION_PATTERN =
            Pattern.compile("\\(MC:\\s*([^\\s)]+)\\)");
    private static final String LORE_PREFIX = "§§";
    private static final boolean IS_PAPER = hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");
    private static final MinecraftVersion MINECRAFT_VERSION;

    static {
        MINECRAFT_VERSION = resolveMinecraftVersion();
        MID_VERSION = MINECRAFT_VERSION.getCompatibilityMajor();
    }

    private static boolean hasClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean isSupportedTool(Material material) {
        LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
        if (instance == null) {
            return false;
        }
        ProfileManager profileManager = instance.getProfileManager();
        if (profileManager == null) {
            return false;
        }
        return profileManager.hasMaterialProfile(material);
    }

    @Nullable
    public static ItemProfile getItemProfile(Material material) {
        LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
        if (instance == null) {
            return null;
        }
        ProfileManager profileManager = instance.getProfileManager();
        if (profileManager == null) {
            return null;
        }
        return profileManager.getProfileForMaterial(material);
    }

    public static ItemStack getHand(Player player) {
        return supportsDualWielding()
                ? player.getInventory().getItemInMainHand().clone()
                : player.getItemInHand().clone();
    }

    public static void setHand(Player player, ItemStack stack) {
        if (supportsDualWielding()) {
            player.getInventory().setItemInMainHand(stack);
        } else {
            player.setItemInHand(stack);
        }
    }

    public static void setItemInSlot(@NotNull Player player, @Nullable TriggerSlot slot, @NotNull ItemStack stack) {
        if (slot == null || slot == TriggerSlot.HAND) {
            setHand(player, stack);
            return;
        }

        if (slot == TriggerSlot.OFF_HAND) {
            if (supportsDualWielding()) {
                player.getInventory().setItemInOffHand(stack);
            } else {
                setHand(player, stack);
            }
            return;
        }

        switch (slot) {
            case HELMET:
                player.getInventory().setHelmet(stack);
                return;
            case CHESTPLATE:
                player.getInventory().setChestplate(stack);
                return;
            case LEGGINGS:
                player.getInventory().setLeggings(stack);
                return;
            case BOOTS:
                player.getInventory().setBoots(stack);
                return;
            default:
                setHand(player, stack);
        }
    }

    public static String createProgressBar(double xp, double maxXp, @Nullable DisplayProfile displayProfile) {
        if (displayProfile != null) {
            return displayProfile.getProgressBar().buildProgressBar(xp, maxXp);
        }
        return ProgressBarConfig.defaultConfig().buildProgressBar(xp, maxXp);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public static int roundDown(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(1, RoundingMode.DOWN);

        return bd.intValue();
    }

    public static LevelToolsItem createLevelToolsItem(ItemStack stack) {
        if (supportsPersistentDataContainer()) {
            if (shouldCheckLegacyNbtData()) {
                final NBTItem nbt = new NBTItem(stack);
                if (nbt.getKeys().stream().anyMatch(s -> s.startsWith("levelTools"))) {
                    return new NBTLevelToolsItem(
                            stack); // Support tools created with "old" NBT system for 1.14+.
                }
            }

            return new PDCLevelToolsItem(stack);
        } else {
            return new NBTLevelToolsItem(stack);
        }
    }

    public static String getServerVersion() {
        String version = Bukkit.getVersion();
        String[] split = version.split(" ");
        return split[split.length - 1].trim().replace(")", "");
    }

    public static boolean requiresLegacyAnvilListener() {
        return isMinecraftVersionBefore(1, 9);
    }

    public static boolean supportsDualWielding() {
        return isMinecraftVersionAtLeast(1, 9);
    }

    public static boolean supportsTranslatableItemDisplayNames() {
        return isMinecraftVersionAtLeast(1, 13);
    }

    public static boolean supportsBlockData() {
        return isMinecraftVersionAtLeast(1, 13);
    }

    public static boolean supportsPersistentDataContainer() {
        return isMinecraftVersionAtLeast(1, 14);
    }

    public static boolean shouldCheckLegacyNbtData() {
        return supportsPersistentDataContainer() && isMinecraftVersionBefore(1, 18);
    }

    public static boolean supportsSpigotActionBar() {
        return isMinecraftVersionAtLeast(1, 13);
    }

    private static boolean isMinecraftVersionAtLeast(int major, int minor) {
        return MINECRAFT_VERSION.compareTo(new MinecraftVersion(major, minor, 0)) >= 0;
    }

    private static boolean isMinecraftVersionBefore(int major, int minor) {
        return MINECRAFT_VERSION.compareTo(new MinecraftVersion(major, minor, 0)) < 0;
    }

    private static MinecraftVersion resolveMinecraftVersion() {
        final String minecraftVersion = getMinecraftVersion();
        if (minecraftVersion != null) {
            return parseMinecraftVersion(minecraftVersion);
        }

        final String serverVersion = getMinecraftVersionFromServerVersion(Bukkit.getVersion());
        if (serverVersion != null) {
            return parseMinecraftVersion(serverVersion);
        }

        return parseMinecraftVersion(Bukkit.getBukkitVersion());
    }

    @Nullable
    private static String getMinecraftVersion() {
        try {
            final Method method = Bukkit.class.getMethod("getMinecraftVersion");
            final Object version = method.invoke(null);
            if (version instanceof String) {
                final String value = ((String) version).trim();
                if (!value.isEmpty()) {
                    return value;
                }
            }
        } catch (ReflectiveOperationException | SecurityException ignored) {
        }

        return null;
    }

    @Nullable
    private static String getMinecraftVersionFromServerVersion(@NotNull String version) {
        final Matcher matcher = SERVER_MINECRAFT_VERSION_PATTERN.matcher(version);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1);
    }

    static MinecraftVersion parseMinecraftVersion(@NotNull String version) {
        final Matcher matcher = MINECRAFT_VERSION_PATTERN.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalStateException("Unable to parse Bukkit Minecraft version: " + version);
        }

        return new MinecraftVersion(
                Integer.parseInt(matcher.group(1)),
                parseVersionPart(matcher.group(2)),
                parseVersionPart(matcher.group(3))
        );
    }

    private static int parseVersionPart(@Nullable String value) {
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public static ItemStack buildItemStack(
            ItemStack stack, Map<Enchantment, Integer> enchantments, int level, double xp, double maxXp) {
        DisplayProfile displayProfile = getDisplayProfileForMaterial(stack.getType());
        return buildItemStack(stack, enchantments, level, xp, maxXp, displayProfile);
    }

    public static ItemStack buildItemStack(
            ItemStack stack, Map<Enchantment, Integer> enchantments, int level, double xp, double maxXp,
            @Nullable DisplayProfile displayProfile) {

        final ItemMeta meta = stack.getItemMeta();
        assert meta != null : "ItemMeta is null! Should not happen.";
        final String progressBar = createProgressBar(xp, maxXp, displayProfile);

        if (displayProfile != null) {
            DisplayProfile.NameDisplay nameDisplay = displayProfile.getNameDisplay();
            if (nameDisplay.isEnabled()) {
                final String text = colorize(nameDisplay.getText()
                        .replace("{level}", String.valueOf(level))
                        .replace("{xp}", String.valueOf(xp))
                        .replace("{max_xp}", String.valueOf(maxXp))
                        .replace("{max_xp_formatted}", formatMoney(maxXp))
                        .replace("{xp_formatted}", formatMoney(xp))
                        .replace("{progress_bar}", progressBar));

                if (nameDisplay.getText().contains("{item}")
                        && supportsTranslatableItemDisplayNames()
                        && IS_PAPER) {
                    AdventureHelper.setDisplayNameWithTranslatable(meta, text, stack);
                } else {
                    meta.setDisplayName(text);
                }
            }

            DisplayProfile.LoreDisplay loreDisplay = displayProfile.getLoreDisplay();
            if (loreDisplay.isEnabled()) {
                List<String> lines = loreDisplay.getLines().stream()
                        .map(str -> LORE_PREFIX + str)
                        .map(str -> colorize(
                                str.replace("{level}", String.valueOf(level))
                                        .replace("{xp}", String.valueOf(xp))
                                        .replace("{max_xp}", String.valueOf(maxXp))
                                        .replace("{progress_bar}", progressBar))
                                .replace("{max_xp_formatted}", formatMoney(maxXp))
                                .replace("{xp_formatted}", formatMoney(xp)))
                        .collect(Collectors.toList());
                smartSetLore(meta, lines);
            }
        }

        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        if (LevelToolsPlugin.getInstance().getConfig().getBoolean("hide_attributes", true)) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        stack.setItemMeta(meta);

        return stack;
    }

    @Nullable
    private static DisplayProfile getDisplayProfileForMaterial(Material material) {
        ItemProfile itemProfile = getItemProfile(material);
        if (itemProfile == null) {
            return null;
        }
        LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
        if (instance == null) {
            return null;
        }
        ProfileManager profileManager = instance.getProfileManager();
        if (profileManager == null) {
            return null;
        }
        return profileManager.getDisplayProfileFor(itemProfile);
    }

    private static void smartSetLore(@NotNull ItemMeta meta, @NotNull List<String> toAdd) {
        final List<String> lore = meta.getLore();
        if (!meta.hasLore() || lore == null) {
            meta.setLore(toAdd);

            return;
        }

        final int[] bounds = findPrefixBounds(lore);
        final int start = bounds[0];
        final int end = bounds[1];
        if (start == -1) {
            lore.addAll(toAdd);
            meta.setLore(lore);

            return;
        }
        if (end >= lore.size()) {
            meta.setLore(toAdd);

            return;
        }
        final List<String> sub = lore.subList(start, end + 1);
        sub.clear();
        sub.addAll(toAdd);
        meta.setLore(lore);
    }

    private static int[] findPrefixBounds(@NotNull List<String> lore) {
        final int[] arr = new int[]{-1, -1};
        for (int i = 0; i < lore.size(); i++) {
            final String text = lore.get(i);
            final String line = decolorize(text);
            if (line.startsWith("&&")) { // LORE_PREFIX gets turned into && if decolorized. Hopefully this doesn't clash with other plugins :/
                if (arr[0] == -1) {
                    arr[0] = i;
                }

                arr[1] = i;
            }
        }

        return arr;
    }

    public static void handleReward(LevelToolsItem tool, Player player) {
        Material material = tool.getItemStack().getType();
        ItemProfile itemProfile = getItemProfile(material);
        if (itemProfile == null) {
            return;
        }

        LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
        if (instance == null) {
            return;
        }

        ProfileManager profileManager = instance.getProfileManager();
        if (profileManager == null) {
            return;
        }

        RewardProfile rewardProfile = profileManager.getRewardProfileFor(itemProfile);
        if (rewardProfile == null) {
            return;
        }

        int level = tool.getLevel();
        if (!rewardProfile.hasRewardsForLevel(level)) {
            return;
        }

        if (tool.getLastHandledReward() == level) {
            return;
        }

        tool.setLastHandledReward(level);
        setHand(player, tool.getItemStack());

        List<RewardEntry> rewards = rewardProfile.getRewardsForLevel(level);
        for (RewardEntry entry : rewards) {
            entry.apply(tool, player);
            if (entry.shouldUpdateItem()) {
                setHand(player, tool.getItemStack());
            }
        }
    }

    public static void sendActionBar(Player player, String msg) {
        if (supportsSpigotActionBar()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
        } else {
            ActionBar.sendActionBar(player, msg);
        }
    }

    public static Scheduler createScheduler(LevelToolsPlugin plugin) {
        if (isFolia()) {
            return new FoliaScheduler(plugin);
        }

        return new BukkitScheduler(plugin);
    }

    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static final class MinecraftVersion implements Comparable<MinecraftVersion> {
        private final int major;
        private final int minor;
        private final int patch;

        private MinecraftVersion(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        private int getCompatibilityMajor() {
            if (major == 1) {
                return minor;
            }
            return major;
        }

        @Override
        public int compareTo(@NotNull MinecraftVersion other) {
            if (major != other.major) {
                return Integer.compare(major, other.major);
            }
            if (minor != other.minor) {
                return Integer.compare(minor, other.minor);
            }
            return Integer.compare(patch, other.patch);
        }
    }
}
