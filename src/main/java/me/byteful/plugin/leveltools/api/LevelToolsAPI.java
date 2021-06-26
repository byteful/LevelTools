package me.byteful.plugin.leveltools.api;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.inventory.ItemStack;

public interface LevelToolsAPI {
  static LevelToolsAPI instance() {
    return LevelToolsPlugin.getInstance();
  }

  LevelToolsItem createLevelToolsItem(ItemStack stack);

  LevelToolsItem fromItemStack(ItemStack stack);
}
