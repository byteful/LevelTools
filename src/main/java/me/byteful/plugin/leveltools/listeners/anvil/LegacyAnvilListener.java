package me.byteful.plugin.leveltools.listeners.anvil;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.AnvilCombineMode;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.model.LevelAndXPModel;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class LegacyAnvilListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvilCombine(InventoryClickEvent e) {
        if (!(e.getInventory() instanceof AnvilInventory)) return;

        final AnvilInventory inv = ((AnvilInventory) e.getInventory());
        final ItemStack firstItem = inv.getItem(0);
        final ItemStack secondItem = inv.getItem(1);
        final ItemStack result = inv.getItem(2);

        if (result == null
                || !LevelToolsUtil.isSupportedTool(result.getType())
                || firstItem == null
                || secondItem == null
                || !LevelToolsUtil.isSupportedTool(firstItem.getType())
                || !LevelToolsUtil.isSupportedTool(secondItem.getType())) {
            return;
        }

        final AnvilCombineMode mode = LevelToolsPlugin.getInstance().getAnvilCombineMode();
        final LevelAndXPModel first =
                LevelAndXPModel.fromItem(LevelToolsUtil.createLevelToolsItem(firstItem));
        final LevelAndXPModel second =
                LevelAndXPModel.fromItem(LevelToolsUtil.createLevelToolsItem(secondItem));
        final LevelAndXPModel finished = mode.getHandler().apply(first, second);
        final LevelToolsItem finalItem = LevelToolsUtil.createLevelToolsItem(result);
        finalItem.setLevel(finished.getLevel());
        finalItem.setXp(finished.getXp());

        inv.setItem(2, finalItem.getItemStack());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvilRepair(InventoryClickEvent e) {
        if (!(e.getInventory() instanceof AnvilInventory)) return;

        final AnvilInventory inv = (AnvilInventory) e.getInventory();
        final ItemStack firstItem = inv.getItem(0);
        final ItemStack secondItem = inv.getItem(1);
        final ItemStack result = inv.getItem(2);

        if (result == null
                || !LevelToolsUtil.isSupportedTool(result.getType())
                || firstItem == null
                || secondItem == null
                || !LevelToolsUtil.isSupportedTool(firstItem.getType())) {
            return;
        }

        if (LevelToolsUtil.isSupportedTool(secondItem.getType())) {
            return;
        }

        final LevelToolsItem original = LevelToolsUtil.createLevelToolsItem(firstItem);
        final LevelToolsItem finalItem = LevelToolsUtil.createLevelToolsItem(result);
        finalItem.setLevel(original.getLevel());
        finalItem.setXp(original.getXp());
        finalItem.setLastHandledReward(original.getLastHandledReward());

        inv.setItem(2, finalItem.getItemStack());
    }
}
