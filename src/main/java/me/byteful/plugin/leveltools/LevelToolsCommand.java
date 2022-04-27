package me.byteful.plugin.leveltools;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import redempt.redlib.commandmanager.Messages;

@CommandAlias("leveltools")
public class LevelToolsCommand extends BaseCommand {
  @Dependency private LevelToolsPlugin plugin;

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
    sender.sendMessage(Messages.msg("successfulReload"));
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
      player.sendMessage(Messages.msg("successfullyExecutedAction"));
    } else {
      player.sendMessage(Messages.msg("itemNotTool"));
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
      player.sendMessage(Messages.msg("successfullyExecutedAction"));
    } else {
      player.sendMessage(Messages.msg("itemNotTool"));
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
      player.sendMessage(Messages.msg("successfullyExecutedAction"));
    } else {
      player.sendMessage(Messages.msg("itemNotTool"));
    }
  }

  private boolean checkPerm(CommandSender sender) {
    if (!sender.hasPermission("leveltools.admin")) {
      sender.sendMessage(Messages.msg("noPermission"));
      return false;
    }
    return true;
  }
}
