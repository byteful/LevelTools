package me.byteful.plugin.leveltools;

import static me.byteful.plugin.leveltools.util.Text.colorize;

import java.util.Objects;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import revxrsal.commands.annotation.*;
import revxrsal.commands.help.CommandHelp;

@Command("leveltools")
public class LevelToolsCommand {
  @Dependency private LevelToolsPlugin plugin;

  @DefaultFor("leveltools")
  @Subcommand("help")
  @Description("Shows the list of LevelTools commands.")
  public void onHelp(CommandSender sender, CommandHelp<String> help, @Default("1") int page) {
    sender.sendMessage(colorize("&6&lLevelTools Command Help:"));
    for (String entry : help.paginate(page, 7)) {
      sender.sendMessage(colorize(entry));
    }
  }

  @Subcommand("reload")
  @Description("Reloads LevelTools' plugin configuration.")
  public void onReload(CommandSender sender) {
    if (!checkPerm(sender)) {
      return;
    }

    plugin.reloadConfig();
    plugin.setAnvilCombineMode();
    plugin.setLevelXpFormula();
    sender.sendMessage(
        colorize(
            Objects.requireNonNull(plugin.getConfig().getString("messages.successful_reload"))));
  }

  @Subcommand("reset")
  @Description("Resets all XP/Levels for all the items in the target player.")
  public void onReset(CommandSender sender, Player target, @Switch("all") boolean all) {
    final ItemStack hand = LevelToolsUtil.getHand(target);
    if (!all) {
      if (!LevelToolsUtil.isSupportedTool(hand.getType())) {
        sender.sendMessage(colorize(plugin.getConfig().getString("messages.item_not_tool")));

        return;
      }

      final LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(hand);
      tool.setLevel(0);
      tool.setXp(0);
      LevelToolsUtil.setHand(target, tool.getItemStack());
      sender.sendMessage(
          colorize(
              plugin
                  .getConfig()
                  .getString(
                      "messages.successfully_reset_hand_tool",
                      "&aSuccessfully reset tool in hand's XP/Levels for {player}.")
                  .replace("{player}", target.getName())));

      return;
    }

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
    sender.sendMessage(
        colorize(
            Objects.requireNonNull(
                    plugin.getConfig().getString("messages.successfully_reset_tools"))
                .replace("{player}", target.getName())));
  }

  @Subcommand("xp")
  @Description("Sets the item in hand's XP to provided XP.")
  public void onXP(Player player, double xp) {
    if (!checkPerm(player)) {
      return;
    }

    final ItemStack item = LevelToolsUtil.getHand(player);

    if (LevelToolsUtil.isSupportedTool(item.getType())) {
      final LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
      tool.setXp(xp);
      LevelToolsUtil.setHand(player, tool.getItemStack());
      player.sendMessage(
          colorize(
              Objects.requireNonNull(
                  plugin.getConfig().getString("messages.successfully_executed_action"))));
    } else {
      player.sendMessage(
          colorize(Objects.requireNonNull(plugin.getConfig().getString("messages.item_not_tool"))));
    }
  }

  @Subcommand("level")
  @Description("Sets the item in hand's level to provided level.")
  public void onLevel(Player player, int level) {
    if (!checkPerm(player)) {
      return;
    }

    final ItemStack item = LevelToolsUtil.getHand(player);

    if (LevelToolsUtil.isSupportedTool(item.getType())) {
      final LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
      final int initial = tool.getLevel();
      tool.setLevel(level);
      LevelToolsUtil.setHand(player, tool.getItemStack());
      if (initial != tool.getLevel()) {
        LevelToolsUtil.handleReward(tool, player);
      }
      player.sendMessage(
          colorize(
              Objects.requireNonNull(
                  plugin.getConfig().getString("messages.successfully_executed_action"))));
    } else {
      player.sendMessage(
          colorize(Objects.requireNonNull(plugin.getConfig().getString("messages.item_not_tool"))));
    }
  }

  @Subcommand("levelup")
  @Description("Increases the item in hand's level to next level.")
  public void onLevelUp(Player player) {
    if (!checkPerm(player)) {
      return;
    }

    final ItemStack item = LevelToolsUtil.getHand(player);

    if (LevelToolsUtil.isSupportedTool(item.getType())) {
      final LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(item);
      tool.setLevel(tool.getLevel() + 1);
      LevelToolsUtil.setHand(player, tool.getItemStack());
      LevelToolsUtil.handleReward(tool, player);
      player.sendMessage(
          colorize(
              Objects.requireNonNull(
                  plugin.getConfig().getString("messages.successfully_executed_action"))));
    } else {
      player.sendMessage(
          colorize(Objects.requireNonNull(plugin.getConfig().getString("messages.item_not_tool"))));
    }
  }

  private boolean checkPerm(CommandSender sender) {
    if (!sender.hasPermission("leveltools.admin")) {
      sender.sendMessage(
          colorize(Objects.requireNonNull(plugin.getConfig().getString("messages.no_permission"))));

      return false;
    }

    return true;
  }
}
