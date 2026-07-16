package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.trigger.*;
import me.byteful.plugin.leveltools.api.trigger.impl.FarmingTrigger;
import me.byteful.plugin.leveltools.config.ConfigManager;
import me.byteful.plugin.leveltools.profile.ProfileManager;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.profile.trigger.TriggerProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class TriggerListener implements Listener {
    private final ProfileManager profileManager;
    private final TriggerRegistry triggerRegistry;
    private final XPHandler xpHandler;

    public TriggerListener(
            @NotNull ProfileManager profileManager,
            @NotNull TriggerRegistry triggerRegistry
    ) {
        this.profileManager = profileManager;
        this.triggerRegistry = triggerRegistry;
        this.xpHandler = new XPHandler(profileManager);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        ConfigManager.Settings settings = LevelToolsPlugin.getInstance().getConfigManager().getSettings();
        if (!settings.isCountPlayerPlacedBlocks()
                && LevelToolsPlugin.getInstance().getBlockDataManager().isPlacedBlock(BlockPosition.fromBukkit(block))) {
            if (settings.isIgnorePlayerPlacedBlocksForFullyGrownCrops() && FarmingTrigger.isMatureCropSource(block)) {
                handleTrigger(player, player.getItemInHand(), TriggerSlot.HAND, block, event, TriggerIds.FARMING);
            }
            return;
        }

        handleTrigger(player, player.getItemInHand(), TriggerSlot.HAND, block, event, TriggerIds.BLOCK_BREAK, TriggerIds.FARMING);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        if (!killer.hasPermission("leveltools.enabled")) {
            return;
        }

        handleTrigger(killer, killer.getItemInHand(), TriggerSlot.HAND, event.getEntity(), event, TriggerIds.ENTITY_KILL);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        Entity caught = event.getCaught();
        ItemStack item;
        TriggerSlot slot;

        if (!LevelToolsUtil.supportsDualWielding() || event.getHand() == null) {
            item = event.getPlayer().getItemInHand();
            slot = TriggerSlot.HAND;
        } else {
            item = event.getPlayer().getInventory().getItem(event.getHand());
            slot = TriggerSlot.fromBukkit(event.getHand());
        }

        handleTrigger(player, item, slot, caught, event, TriggerIds.FISHING);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action == Action.PHYSICAL) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null && event.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        TriggerSlot slot = !LevelToolsUtil.supportsDualWielding() || event.getHand() == null
                ? TriggerSlot.HAND
                : TriggerSlot.fromBukkit(event.getHand());

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            handleTrigger(player, item, slot, clickedBlock, event, TriggerIds.RIGHT_CLICK, TriggerIds.FARMING);
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            handleTrigger(player, item, slot, clickedBlock, event, TriggerIds.LEFT_CLICK);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        TriggerSlot slot = !LevelToolsUtil.supportsDualWielding()
                ? TriggerSlot.HAND
                : TriggerSlot.fromBukkit(event.getHand());
        ItemStack consumedItem = event.getItem();
        handleTrigger(player, consumedItem, slot, consumedItem, event, TriggerIds.CONSUME);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        ItemStack damagedItem = event.getItem();
        if (damagedItem.getType() == Material.AIR) {
            return;
        }

        TriggerSlot armorSlot = findArmorSlot(player, damagedItem);
        if (armorSlot == null) {
            return;
        }

        handleTrigger(player, damagedItem, armorSlot, damagedItem.getType(), event, TriggerIds.ARMOR_DURABILITY);
    }

    private void handleTrigger(
            @NotNull Player player,
            @NotNull ItemStack item,
            @Nullable TriggerSlot slot,
            @Nullable Object source,
            @NotNull Event event,
            @NotNull String... triggerIds
    ) {
        if (item.getType() == Material.AIR) {
            return;
        }

        ConfigManager.Settings settings = LevelToolsPlugin.getInstance().getConfigManager().getSettings();
        if (settings.getDisabledWorlds().contains(player.getWorld().getName())) {
            return;
        }

        ItemProfile itemProfile = profileManager.getProfileForMaterial(item.getType());
        if (itemProfile == null) {
            return;
        }

        List<TriggerProfile> triggerProfiles = profileManager.getTriggerProfilesFor(itemProfile);
        if (triggerProfiles.isEmpty()) {
            return;
        }

        double totalModifier = 0.0;
        for (TriggerProfile triggerProfile : triggerProfiles) {
            String triggerId = triggerProfile.getTriggerId();
            if (!isApplicableTrigger(triggerId, triggerIds)) {
                continue;
            }

            if (!triggerProfile.getSlotFilter().matches(slot)) {
                continue;
            }

            Trigger trigger = triggerRegistry.get(triggerId);
            if (trigger == null) {
                continue;
            }

            TriggerContext context = TriggerContext.builder()
                    .player(player)
                    .item(item)
                    .triggerId(triggerId)
                    .slot(slot)
                    .source(source)
                    .originalEvent(event)
                    .itemProfile(itemProfile)
                    .triggerProfile(triggerProfile)
                    .build();

            if (!trigger.canHandle(context)) {
                continue;
            }

            totalModifier += trigger.calculateXpModifier(context);
        }

        if (totalModifier <= 0.0) {
            return;
        }

        LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
        xpHandler.handle(player, itemProfile, slot, tool, totalModifier, event instanceof PlayerItemDamageEvent);
    }

    private boolean isApplicableTrigger(@NotNull String triggerId, @NotNull String[] triggerIds) {
        for (String id : triggerIds) {
            if (id.equals(triggerId)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private TriggerSlot findArmorSlot(@NotNull Player player, @NotNull ItemStack itemStack) {
        if (matchesItem(player.getInventory().getHelmet(), itemStack)) {
            return TriggerSlot.HELMET;
        }

        if (matchesItem(player.getInventory().getChestplate(), itemStack)) {
            return TriggerSlot.CHESTPLATE;
        }

        if (matchesItem(player.getInventory().getLeggings(), itemStack)) {
            return TriggerSlot.LEGGINGS;
        }

        if (matchesItem(player.getInventory().getBoots(), itemStack)) {
            return TriggerSlot.BOOTS;
        }

        return null;
    }

    private boolean matchesItem(@Nullable ItemStack item, @NotNull ItemStack check) {
        return item != null && item.equals(check);
    }
}
