package me.byteful.plugin.leveltools.listeners;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.LevelToolsUtil;
import me.byteful.plugin.leveltools.api.AnvilCombineMode;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.model.LevelAndXPModel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilListener implements Listener {
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onAnvilCombine(PrepareAnvilEvent e) {
    final AnvilInventory inv = e.getInventory();
    final ItemStack firstItem = inv.getItem(0);
    final ItemStack secondItem = inv.getItem(1);
    final ItemStack result = e.getResult();

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

    e.setResult(finalItem.getItemStack());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onAnvilRepair(PrepareAnvilEvent e) {
    final AnvilInventory inv = e.getInventory();
    final ItemStack firstItem = inv.getItem(0);
    final ItemStack secondItem = inv.getItem(1);
    final ItemStack result = e.getResult();

    if (result == null
        || !LevelToolsUtil.isSupportedTool(result.getType())
        || firstItem == null
        || secondItem == null
        || !LevelToolsUtil.isSupportedTool(firstItem.getType())) {
      return;
    }

    final LevelToolsItem finalItem = LevelToolsUtil.createLevelToolsItem(result);
    e.setResult(finalItem.getItemStack()); // This has to be done to patch lore issues.
  }
}
