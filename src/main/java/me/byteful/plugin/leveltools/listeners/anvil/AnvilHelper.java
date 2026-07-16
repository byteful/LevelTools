package me.byteful.plugin.leveltools.listeners.anvil;

import me.byteful.plugin.leveltools.LevelToolsPlugin;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class AnvilHelper {
    private AnvilHelper() {}

    static ItemStack getResultItem(LevelToolsItem item, HumanEntity viewer, ItemStack result) {
        final Player player = viewer instanceof Player ? (Player) viewer : null;
        final ItemProfile profile = LevelToolsUtil.getItemProfile(result.getType());

        return LevelToolsUtil.getItemStack(item, player, profile);
    }

    static boolean shouldBlockEnchantedBookResult(ItemStack firstItem, ItemStack secondItem, ItemStack result) {
        return LevelToolsPlugin.getInstance().getConfigManager().getSettings().isPreventEnchantedBooksOnLevelToolsItems()
            && result != null
            && firstItem != null
            && secondItem != null
            && LevelToolsUtil.isSupportedTool(result.getType())
            && LevelToolsUtil.isSupportedTool(firstItem.getType())
            && secondItem.getType() == Material.ENCHANTED_BOOK;
    }
}
