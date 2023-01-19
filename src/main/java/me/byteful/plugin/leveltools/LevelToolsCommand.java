package me.byteful.plugin.leveltools;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import java.util.Objects;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import me.byteful.plugin.leveltools.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

@CommandAlias("leveltools")
public class LevelToolsCommand extends BaseCommand {
  @Dependency
  private LevelToolsPlugin plugin;

  @Default
  @HelpCommand
  @Description("Shows the list of LevelTools commands.")
  public void onHelp(CommandSender sender, CommandHelp help) {
    help.showHelp();
  }

  @Subcommand("reload")
  @Description("Reloads LevelTools' plugin configuration.")
  public void onReload(CommandSender sender) {
    if (!checkPerm(sender)) {
      return;
    }

    plugin.reloadConfig();
    plugin.setAnvilCombineMode();
    sender.sendMessage(
      Text.colorize(
        Objects.requireNonNull(plugin.getConfig().getString("messages.successful_reload"))));
  }

  @Subcommand("reset")
  @Description("Resets all XP/Levels for all the items in the target player.")
  public void onReset(CommandSender sender, Player target) {
    final PlayerInventory inv = target.getInventory();
    final ItemStack[] contents = inv.getContents();
    for (int i = 0; i < contents.length; i++) {
      final ItemStack item = contents[i];
      if (item == null || !LevelToolsUtil.isSupportedTool(item.getType())) {
        continue;
      }
      final LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
      tool.setLevel(0);
      tool.setXp(0);
      inv.setItem(i, tool.getItemStack());
    }
//    if (plugin.getConfig().getString("messages.successfully_reset_tools") == null) {
//      Bukkit.broadcastMessage("the reset tools string was null!");
//    } else {
//      Bukkit.broadcastMessage("it wasn't null.");
//    }
    sender.sendMessage(
      Text.colorize(
        Objects.requireNonNull(plugin.getConfig().getString("messages.successfully_reset_tools"))));
  }

  @Subcommand("xp")
  @Description("Sets the item in hand's XP to provided XP.")
  public void onXP(Player player, double xp) {
    if (!checkPerm(player)) {
      return;
    }

    final ItemStack item = player.getItemInHand();

    if (LevelToolsUtil.isSupportedTool(item.getType())) {
      final LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
      tool.setXp(xp);
      player.setItemInHand(tool.getItemStack());
      player.sendMessage(
        Text.colorize(
          Objects.requireNonNull(
            plugin.getConfig().getString("messages.successfully_executed_action"))));
    } else {
      player.sendMessage(
        Text.colorize(
          Objects.requireNonNull(plugin.getConfig().getString("messages.item_not_tool"))));
    }
  }

  @Subcommand("level")
  @Description("Sets the item in hand's level to provided level.")
  public void onLevel(Player player, int level) {
    if (!checkPerm(player)) {
      return;
    }

    final ItemStack item = player.getItemInHand();

    if (LevelToolsUtil.isSupportedTool(item.getType())) {
      final LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
      final int initial = tool.getLevel();
      tool.setLevel(level);
      if (initial != tool.getLevel()) {
        LevelToolsUtil.handleReward(tool, player);
      }
      player.setItemInHand(tool.getItemStack());
      player.sendMessage(
        Text.colorize(
          Objects.requireNonNull(
            plugin.getConfig().getString("messages.successfully_executed_action"))));
    } else {
      player.sendMessage(
        Text.colorize(
          Objects.requireNonNull(plugin.getConfig().getString("messages.item_not_tool"))));
    }
  }

  @Subcommand("levelup")
  @Description("Increases the item in hand's level to next level.")
  public void onLevelUp(Player player) {
    if (!checkPerm(player)) {
      return;
    }

    final ItemStack item = player.getItemInHand();

    if (LevelToolsUtil.isSupportedTool(item.getType())) {
      final LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
      tool.setLevel(tool.getLevel() + 1);
      LevelToolsUtil.handleReward(tool, player);
      player.setItemInHand(tool.getItemStack());
      player.sendMessage(
        Text.colorize(
          Objects.requireNonNull(
            plugin.getConfig().getString("messages.successfully_executed_action"))));
    } else {
      player.sendMessage(
        Text.colorize(
          Objects.requireNonNull(plugin.getConfig().getString("messages.item_not_tool"))));
    }
  }

  private boolean checkPerm(CommandSender sender) {
    if (!sender.hasPermission("leveltools.admin")) {
      sender.sendMessage(
        Text.colorize(
          Objects.requireNonNull(plugin.getConfig().getString("messages.no_permission"))));

      return false;
    }

    return true;
  }
}
