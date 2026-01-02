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
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.display.DisplayProfile;
import me.byteful.plugin.leveltools.profile.display.ProgressBarConfig;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.reward.RewardEntry;
import me.byteful.plugin.leveltools.profile.reward.RewardProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.byteful.plugin.leveltools.util.Text.*;
import static net.kyori.adventure.text.Component.translatable;

public final class LevelToolsUtil {
    public static final int MID_VERSION;
    private static final String LORE_PREFIX = "§§";
    private static final boolean IS_PAPER = hasClass("com.destroystokyo.paper.PaperConfig") || hasClass("io.papermc.paper.configuration.Configuration");

    static {
        // Code from https://github.com/boxbeam/RedLib/blob/master/src/redempt/redlib/java
        final Pattern pattern = Pattern.compile("1\\.([0-9]+)");
        final Matcher matcher = pattern.matcher(Bukkit.getBukkitVersion());
        matcher.find();
        MID_VERSION = Integer.parseInt(matcher.group(1));
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
        return MID_VERSION >= 9
                ? player.getInventory().getItemInMainHand().clone()
                : player.getItemInHand().clone();
    }

    public static void setHand(Player player, ItemStack stack) {
        if (MID_VERSION >= 9) {
            player.getInventory().setItemInMainHand(stack);
        } else {
            player.setItemInHand(stack);
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
        if (MID_VERSION >= 14) {
            if (MID_VERSION < 18) {
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

                if (nameDisplay.getText().contains("{item}") && IS_PAPER) {
                    final net.kyori.adventure.text.TextComponent component = LegacyComponentSerializer.legacySection().deserialize(text);
                    for (Component child : component.children()) {
                        if (child instanceof net.kyori.adventure.text.TextComponent && ((net.kyori.adventure.text.TextComponent) child).content().equals("{item}")) {
                            ((net.kyori.adventure.text.TextComponent) child).content("");
                            child.append(translatable(stack.getType().translationKey()));
                        }
                    }
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
        if (MID_VERSION > 12) {
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
}
