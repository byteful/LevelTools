package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.api.trigger.Trigger;
import me.byteful.plugin.leveltools.api.trigger.TriggerContext;
import me.byteful.plugin.leveltools.api.trigger.TriggerRegistry;
import me.byteful.plugin.leveltools.api.trigger.TriggerType;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        handleTrigger(TriggerType.BLOCK_BREAK, player, block, event);
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

        handleTrigger(TriggerType.ENTITY_KILL, killer, event.getEntity(), event);
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
        handleTrigger(TriggerType.FISHING, player, caught, event);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            handleTrigger(TriggerType.RIGHT_CLICK, player, clickedBlock, event);
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            handleTrigger(TriggerType.LEFT_CLICK, player, clickedBlock, event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("leveltools.enabled")) {
            return;
        }

        ItemStack consumedItem = event.getItem();
        handleTrigger(TriggerType.CONSUME, player, consumedItem, event);
    }

    private void handleTrigger(
            @NotNull TriggerType type,
            @NotNull Player player,
            @Nullable Object source,
            @NotNull Event event
    ) {
        ItemStack hand = LevelToolsUtil.getHand(player);
        if (hand.getType() == Material.AIR) {
            return;
        }

        ItemProfile itemProfile = profileManager.getProfileForMaterial(hand.getType());
        if (itemProfile == null) {
            return;
        }

        TriggerProfile triggerProfile = profileManager.getTriggerProfileFor(itemProfile);
        if (triggerProfile == null) {
            return;
        }

        if (triggerProfile.getType() != type) {
            return;
        }

        Trigger trigger = triggerRegistry.get(type);
        if (trigger == null) {
            return;
        }

        TriggerContext context = TriggerContext.builder()
                .player(player)
                .item(hand)
                .triggerType(type)
                .source(source)
                .originalEvent(event)
                .itemProfile(itemProfile)
                .triggerProfile(triggerProfile)
                .build();

        if (!trigger.canHandle(context)) {
            return;
        }

        double modifier = trigger.calculateXpModifier(context);

        LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(hand);
        xpHandler.handle(context, tool, modifier);
    }
}
