package me.byteful.plugin.leveltools;

import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.profile.item.ItemProfile;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.help.Help;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static me.byteful.plugin.leveltools.util.Text.colorize;

@Command("leveltools")
public class LevelToolsCommand {
    @Dependency
    private LevelToolsPlugin plugin;

    @CommandPlaceholder
    public void onDefaultHelp(CommandSender sender, Help.RelatedCommands<BukkitCommandActor> help) {
        sendHelp(sender, help, 1);
    }

    @Subcommand("help")
    @Description("Shows the list of LevelTools commands.")
    public void onHelp(CommandSender sender, Help.RelatedCommands<BukkitCommandActor> help, @Default("1") int page) {
        sendHelp(sender, help, page);
    }

    private void sendHelp(CommandSender sender, Help.RelatedCommands<BukkitCommandActor> help, int page) {
        final List<ExecutableCommand<BukkitCommandActor>> entries = new ArrayList<>();
        for (ExecutableCommand<BukkitCommandActor> entry : help) {
            if (entry.description() != null) {
                entries.add(entry);
            }
        }
        sender.sendMessage(colorize("&6&lLevelTools Command Help:"));
        for (ExecutableCommand<BukkitCommandActor> entry : Help.paginate(entries, page, 7)) {
            sender.sendMessage(colorize(format("&7- &b/%s&7: &e%s", entry.usage(), entry.description())));
        }
    }

    @Subcommand("reload")
    @Description("Reloads LevelTools' plugin configuration.")
    public void onReload(CommandSender sender) {
        if (!checkPerm(sender)) {
            return;
        }

        plugin.reloadPlugin();
        sender.sendMessage(
                colorize(
                        Objects.requireNonNull(plugin.getConfig().getString("messages.successful_reload"))));
    }

    @Subcommand("reset")
    @Description("Resets all XP/Levels for all the items in the target player.")
    public void onReset(CommandSender sender, Player target, @Switch("all") boolean all) {
        if (!checkPerm(sender)) {
            return;
        }

        final ItemStack hand = LevelToolsUtil.getHand(target);
        if (!all) {
            if (!LevelToolsUtil.isSupportedTool(hand.getType())) {
                sender.sendMessage(colorize(plugin.getConfig().getString("messages.item_not_tool")));

                return;
            }

            final LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(hand);
            final ItemProfile itemProfile = LevelToolsUtil.getItemProfile(hand.getType());
            tool.setLevel(0);
            tool.setXp(0);
            LevelToolsUtil.setHand(target, LevelToolsUtil.getItemStack(tool, target, itemProfile));
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
            final ItemProfile itemProfile = LevelToolsUtil.getItemProfile(item.getType());
            tool.setLevel(0);
            tool.setXp(0);
            inv.setItem(i, LevelToolsUtil.getItemStack(tool, target, itemProfile));
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
            final ItemProfile itemProfile = LevelToolsUtil.getItemProfile(item.getType());
            tool.setXp(xp);
            LevelToolsUtil.setHand(player, LevelToolsUtil.getItemStack(tool, player, itemProfile));
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
            final ItemProfile itemProfile = LevelToolsUtil.getItemProfile(item.getType());
            final int initial = tool.getLevel();
            tool.setLevel(level);
            LevelToolsUtil.setHand(player, LevelToolsUtil.getItemStack(tool, player, itemProfile));
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
            final ItemProfile itemProfile = LevelToolsUtil.getItemProfile(item.getType());
            tool.setLevel(tool.getLevel() + 1);
            LevelToolsUtil.setHand(player, LevelToolsUtil.getItemStack(tool, player, itemProfile));
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

    @Subcommand("debug")
    @Description("Shows debug information about the server and plugin.")
    public void onDebug(CommandSender sender) {
        if (!checkPerm(sender)) {
            return;
        }

        plugin.getUpdateChecker().check();
        sender.sendMessage("LevelTools Debug Information:");
        sender.sendMessage("- Server Version: " + Bukkit.getVersion());
        sender.sendMessage("- Server Type: " + Bukkit.getBukkitVersion());
        sender.sendMessage("- Plugin Version: " + plugin.getDescription().getVersion());
        sender.sendMessage("- Latest Version: " + plugin.getUpdateChecker().getLastCheckedVersion());
        sender.sendMessage("{!} Please include your configuration with this when asking for help. Please COPY AND PASTE configuration into discord server. {!}");
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
