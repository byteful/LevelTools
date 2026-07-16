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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
    public static final boolean IS_PAPER = hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");
    private static final MinecraftVersion MINECRAFT_VERSION;
    private static final boolean REQUIRES_LEGACY_ANVIL_LISTENER;
    private static final boolean SUPPORTS_DUAL_WIELDING;
    private static final boolean SUPPORTS_TRANSLATABLE_ITEM_DISPLAY_NAMES;
    private static final boolean SUPPORTS_BLOCK_DATA;
    private static final boolean SUPPORTS_PERSISTENT_DATA_CONTAINER;
    private static final boolean SUPPORTS_SPIGOT_ACTION_BAR;

    static {
        MINECRAFT_VERSION = resolveMinecraftVersion();
        MID_VERSION = MINECRAFT_VERSION.getCompatibilityMajor();
        REQUIRES_LEGACY_ANVIL_LISTENER = isMinecraftVersionBefore(1, 9);
        SUPPORTS_DUAL_WIELDING = isMinecraftVersionAtLeast(1, 9);
        SUPPORTS_TRANSLATABLE_ITEM_DISPLAY_NAMES = isMinecraftVersionAtLeast(1, 13);
        SUPPORTS_BLOCK_DATA = isMinecraftVersionAtLeast(1, 13);
        SUPPORTS_PERSISTENT_DATA_CONTAINER = isMinecraftVersionAtLeast(1, 14);
        SUPPORTS_SPIGOT_ACTION_BAR = isMinecraftVersionAtLeast(1, 13);
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

    @Nullable
    public static ItemStack getItemInSlot(@NotNull Player player, @Nullable TriggerSlot slot) {
        if (slot == null || slot == TriggerSlot.HAND) {
            return getHand(player);
        }

        if (slot == TriggerSlot.OFF_HAND) {
            return supportsDualWielding() ? player.getInventory().getItemInOffHand() : getHand(player);
        }

        switch (slot) {
            case HELMET:
                return player.getInventory().getHelmet();
            case CHESTPLATE:
                return player.getInventory().getChestplate();
            case LEGGINGS:
                return player.getInventory().getLeggings();
            case BOOTS:
                return player.getInventory().getBoots();
            default:
                return getHand(player);
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

    public static double getMaxXp(@Nullable Player player, @Nullable ItemProfile itemProfile, @NotNull LevelToolsItem tool) {
        LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
        if (instance == null || instance.getXpFormulaRegistry() == null) {
            return tool.getMaxXp();
        }

        return instance.getXpFormulaRegistry().evaluateMaxXp(player, itemProfile, tool.getLevel());
    }

    @NotNull
    public static ItemStack getItemStack(
            @NotNull LevelToolsItem tool,
            @Nullable Player player,
            @Nullable ItemProfile itemProfile
    ) {
        return tool.getItemStack(getMaxXp(player, itemProfile, tool));
    }

    @Nullable
    public static ItemStack getBackingItemStack(@NotNull LevelToolsItem tool) {
        if (tool instanceof PDCLevelToolsItem) {
            return ((PDCLevelToolsItem) tool).getStack();
        }
        if (tool instanceof NBTLevelToolsItem) {
            return ((NBTLevelToolsItem) tool).getNBT().getItem();
        }
        return null;
    }

    public static boolean rebindLevelToolsItem(@NotNull LevelToolsItem tool, @NotNull ItemStack stack) {
        final int level = tool.getLevel();
        final double xp = tool.getXp();
        final int lastHandledReward = tool.getLastHandledReward();

        if (tool instanceof PDCLevelToolsItem) {
            ((PDCLevelToolsItem) tool).setStack(stack);
        } else if (tool instanceof NBTLevelToolsItem) {
            ((NBTLevelToolsItem) tool).setNBT(new NBTItem(stack));
        } else {
            return false;
        }

        tool.setLevel(level);
        tool.setXp(xp);
        tool.setLastHandledReward(lastHandledReward);

        return true;
    }

    public static LevelToolsItem createLevelToolsItem(ItemStack stack) {
        if (!supportsPersistentDataContainer()) {
            return new NBTLevelToolsItem(stack);
        }

        return isNbtStorageForced() ? createNbtItem(stack) : createPdcItem(stack);
    }

    public static boolean isNbtStorageForced() {
        final LevelToolsPlugin instance = LevelToolsPlugin.getInstance();
        return instance != null && instance.getConfig().getBoolean("force_nbt", false);
    }

    private static LevelToolsItem createNbtItem(ItemStack stack) {
        final ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return new NBTLevelToolsItem(stack);
        }

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        final Integer level = pdc.get(PDCLevelToolsItem.LEVEL_KEY, PersistentDataType.INTEGER);
        final Double xp = pdc.get(PDCLevelToolsItem.XP_KEY, PersistentDataType.DOUBLE);
        final Integer lastReward = pdc.get(PDCLevelToolsItem.LAST_REWARD_KEY, PersistentDataType.INTEGER);
        if (level == null && xp == null && lastReward == null) {
            return new NBTLevelToolsItem(stack);
        }

        pdc.remove(PDCLevelToolsItem.LEVEL_KEY);
        pdc.remove(PDCLevelToolsItem.XP_KEY);
        pdc.remove(PDCLevelToolsItem.LAST_REWARD_KEY);
        final ItemStack migrated = stack.clone();
        migrated.setItemMeta(meta);

        final NBTLevelToolsItem item = new NBTLevelToolsItem(migrated);
        if (level != null) {
            item.setLevel(level);
        }
        if (xp != null) {
            item.setXp(xp);
        }
        if (lastReward != null) {
            item.setLastHandledReward(lastReward);
        }

        return item;
    }

    private static LevelToolsItem createPdcItem(ItemStack stack) {
        final ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return new PDCLevelToolsItem(stack, null);
        }

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(PDCLevelToolsItem.LEVEL_KEY, PersistentDataType.INTEGER)
                || pdc.has(PDCLevelToolsItem.XP_KEY, PersistentDataType.DOUBLE)
                || pdc.has(PDCLevelToolsItem.LAST_REWARD_KEY, PersistentDataType.INTEGER)) {
            return new PDCLevelToolsItem(stack, meta);
        }

        final NBTItem nbt = new NBTItem(stack);
        final boolean hasLevel = nbt.hasTag(NBTLevelToolsItem.LEVEL_KEY);
        final boolean hasXp = nbt.hasTag(NBTLevelToolsItem.XP_KEY);
        final boolean hasLastReward = nbt.hasTag(NBTLevelToolsItem.LAST_REWARD_KEY);
        if (!hasLevel && !hasXp && !hasLastReward) {
            return new PDCLevelToolsItem(stack, meta);
        }

        final Integer level = hasLevel ? nbt.getInteger(NBTLevelToolsItem.LEVEL_KEY) : null;
        final Double xp = hasXp ? nbt.getDouble(NBTLevelToolsItem.XP_KEY) : null;
        final Integer lastReward = hasLastReward ? nbt.getInteger(NBTLevelToolsItem.LAST_REWARD_KEY) : null;
        nbt.removeKey(NBTLevelToolsItem.LEVEL_KEY);
        nbt.removeKey(NBTLevelToolsItem.XP_KEY);
        nbt.removeKey(NBTLevelToolsItem.LAST_REWARD_KEY);

        final PDCLevelToolsItem item = new PDCLevelToolsItem(nbt.getItem());
        if (level != null) {
            item.setLevel(level);
        }
        if (xp != null) {
            item.setXp(xp);
        }
        if (lastReward != null) {
            item.setLastHandledReward(lastReward);
        }

        return item;
    }

    public static String getServerVersion() {
        String version = Bukkit.getVersion();
        String[] split = version.split(" ");
        return split[split.length - 1].trim().replace(")", "");
    }

    public static boolean requiresLegacyAnvilListener() {
        return REQUIRES_LEGACY_ANVIL_LISTENER;
    }

    public static boolean supportsDualWielding() {
        return SUPPORTS_DUAL_WIELDING;
    }

    public static boolean supportsTranslatableItemDisplayNames() {
        return SUPPORTS_TRANSLATABLE_ITEM_DISPLAY_NAMES;
    }

    public static boolean supportsBlockData() {
        return SUPPORTS_BLOCK_DATA;
    }

    public static boolean supportsPersistentDataContainer() {
        return SUPPORTS_PERSISTENT_DATA_CONTAINER;
    }

    public static boolean supportsSpigotActionBar() {
        return SUPPORTS_SPIGOT_ACTION_BAR;
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
        final ItemMeta meta = stack.getItemMeta();
        assert meta != null : "ItemMeta is null! Should not happen.";
        applyDisplay(stack, meta, enchantments, level, xp, maxXp);
        stack.setItemMeta(meta);

        return stack;
    }

    public static void applyDisplay(
            ItemStack stack, ItemMeta meta, Map<Enchantment, Integer> enchantments, int level, double xp,
            double maxXp) {
        final DisplayProfile displayProfile = getDisplayProfileForMaterial(stack.getType());
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
        if (LevelToolsPlugin.getInstance().getConfigManager().getSettings().isHideAttributes()) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
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
            if (lore.get(i).startsWith(LORE_PREFIX)) {
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

        if (applyRewards(tool, player, rewardProfile)) {
            setHand(player, getItemStack(tool, player, itemProfile));
        }
    }

    public static boolean applyRewards(
            @NotNull LevelToolsItem tool, @NotNull Player player, @NotNull RewardProfile rewardProfile) {
        int level = tool.getLevel();
        if (!rewardProfile.hasRewardsForLevel(level) || tool.getLastHandledReward() == level) {
            return false;
        }

        tool.setLastHandledReward(level);
        for (RewardEntry entry : rewardProfile.getRewardsForLevel(level)) {
            entry.apply(tool, player);
        }

        return true;
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
