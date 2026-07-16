package me.byteful.plugin.leveltools.listeners.anvil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import static me.byteful.plugin.leveltools.listeners.anvil.AnvilHelper.shouldBlockEnchantedBookResult;
import static me.byteful.plugin.leveltools.listeners.anvil.LegacyAnvilListener.createUpdatedResult;
import static me.byteful.plugin.leveltools.listeners.anvil.LegacyAnvilListener.isHandledAnvilAction;

public class AnvilListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        final AnvilInventory inv = e.getInventory();
        final ItemStack firstItem = inv.getItem(0);
        final ItemStack secondItem = inv.getItem(1);
        final ItemStack result = e.getResult();

        if (shouldBlockEnchantedBookResult(firstItem, secondItem, result)) {
            e.setResult(null);
            return;
        }

        if (!isHandledAnvilAction(firstItem, secondItem, result)) {
            return;
        }

        e.setResult(createUpdatedResult(firstItem, secondItem, result, e.getView().getPlayer()));
    }
}
