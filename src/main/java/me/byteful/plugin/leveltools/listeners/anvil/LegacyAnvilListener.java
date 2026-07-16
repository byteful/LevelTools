package me.byteful.plugin.leveltools.listeners.anvil;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.AnvilCombineMode;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.model.LevelAndXPModel;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import static me.byteful.plugin.leveltools.listeners.anvil.AnvilHelper.getResultItem;
import static me.byteful.plugin.leveltools.listeners.anvil.AnvilHelper.shouldBlockEnchantedBookResult;

public class LegacyAnvilListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvilClick(InventoryClickEvent e) {
        if (!(e.getInventory() instanceof AnvilInventory)) return;

        final int rawSlot = e.getRawSlot();
        if (rawSlot != 0 && rawSlot != 1 && rawSlot != 2) return;

        final AnvilInventory inv = (AnvilInventory) e.getInventory();
        final ItemStack firstItem = inv.getItem(0);
        final ItemStack secondItem = inv.getItem(1);
        final ItemStack result = inv.getItem(2);

        if (shouldBlockEnchantedBookResult(firstItem, secondItem, result)) {
            if (rawSlot == 2) {
                e.setCancelled(true);
            }
            inv.setItem(2, null);
            return;
        }

        if (!isHandledAnvilAction(firstItem, secondItem, result)) {
            return;
        }

        inv.setItem(2, createUpdatedResult(firstItem, secondItem, result, e.getWhoClicked()));
    }

    static boolean isHandledAnvilAction(ItemStack firstItem, ItemStack secondItem, ItemStack result) {
        return result != null
                && LevelToolsUtil.isSupportedTool(result.getType())
                && firstItem != null
                && secondItem != null
                && LevelToolsUtil.isSupportedTool(firstItem.getType());
    }

    static ItemStack createUpdatedResult(
            ItemStack firstItem, ItemStack secondItem, ItemStack result, HumanEntity viewer) {
        final LevelToolsItem finalItem = LevelToolsUtil.createLevelToolsItem(result);

        if (LevelToolsUtil.isSupportedTool(secondItem.getType())) {
            final AnvilCombineMode mode = LevelToolsPlugin.getInstance().getAnvilCombineMode();
            final LevelAndXPModel first =
                    LevelAndXPModel.fromItem(LevelToolsUtil.createLevelToolsItem(firstItem));
            final LevelAndXPModel second =
                    LevelAndXPModel.fromItem(LevelToolsUtil.createLevelToolsItem(secondItem));
            final LevelAndXPModel finished = mode.getHandler().apply(first, second);
            finalItem.setLevel(finished.getLevel());
            finalItem.setXp(finished.getXp());
        } else {
            final LevelToolsItem original = LevelToolsUtil.createLevelToolsItem(firstItem);
            finalItem.setLevel(original.getLevel());
            finalItem.setXp(original.getXp());
            finalItem.setLastHandledReward(original.getLastHandledReward());
        }

        return getResultItem(finalItem, viewer, result);
    }
}
