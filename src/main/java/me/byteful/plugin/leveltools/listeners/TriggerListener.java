package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.trigger.*;
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
    private final BlockDataManager blockDataManager;

    public TriggerListener(
            @NotNull ProfileManager profileManager,
            @NotNull TriggerRegistry triggerRegistry,
            @NotNull BlockDataManager blockDataManager
    ) {
        this.profileManager = profileManager;
        this.triggerRegistry = triggerRegistry;
        this.xpHandler = new XPHandler(profileManager);
        this.blockDataManager = blockDataManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        if (!LevelToolsPlugin.getInstance().getConfig().getBoolean("playerPlacedBlocks")
                && blockDataManager.isPlacedBlock(BlockPosition.fromBukkit(block))) {
            return;
        }

        handleTrigger(TriggerIds.BLOCK_BREAK, player, player.getItemInHand(), TriggerSlot.HAND, block, event);
        handleTrigger(TriggerIds.FARMING, player, player.getItemInHand(), TriggerSlot.HAND, block, event);
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

        handleTrigger(TriggerIds.ENTITY_KILL, killer, killer.getItemInHand(), TriggerSlot.HAND, event.getEntity(), event);
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

        if (LevelToolsUtil.MID_VERSION <= 8 || event.getHand() == null) {
            item = event.getPlayer().getItemInHand();
            slot = TriggerSlot.HAND;
        } else {
            item = event.getPlayer().getInventory().getItem(event.getHand());
            slot = TriggerSlot.fromBukkit(event.getHand());
        }

        handleTrigger(TriggerIds.FISHING, player, item, slot, caught, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();
        TriggerSlot slot = LevelToolsUtil.MID_VERSION <= 8 || event.getHand() == null
                ? TriggerSlot.HAND
                : TriggerSlot.fromBukkit(event.getHand());
        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            handleTrigger(TriggerIds.RIGHT_CLICK, player, item, slot, clickedBlock, event);
            handleTrigger(TriggerIds.FARMING, player, item, slot, clickedBlock, event);
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            handleTrigger(TriggerIds.LEFT_CLICK, player, item, slot, clickedBlock, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        TriggerSlot slot = LevelToolsUtil.MID_VERSION <= 8 ? TriggerSlot.HAND : TriggerSlot.fromBukkit(event.getHand());
        ItemStack consumedItem = event.getItem();
        handleTrigger(TriggerIds.CONSUME, player, consumedItem, slot, consumedItem, event);
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

        handleTrigger(TriggerIds.ARMOR_DURABILITY, player, damagedItem, armorSlot, damagedItem.getType(), event);
    }

    private void handleTrigger(
            @NotNull String triggerId,
            @NotNull Player player,
            @NotNull ItemStack item,
            @Nullable TriggerSlot slot,
            @Nullable Object source,
            @NotNull Event event
    ) {
        if (item.getType() == Material.AIR) {
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

        Trigger trigger = triggerRegistry.get(triggerId);
        if (trigger == null) {
            return;
        }

        double totalModifier = 0.0;
        for (TriggerProfile triggerProfile : triggerProfiles) {
            if (!triggerId.equals(triggerProfile.getTriggerId())) {
                continue;
            }

            if (!triggerProfile.getSlotFilter().matches(slot)) {
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
        xpHandler.handle(player, itemProfile, slot, tool, totalModifier);
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
