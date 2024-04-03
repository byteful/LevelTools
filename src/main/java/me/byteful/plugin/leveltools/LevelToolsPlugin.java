package me.byteful.plugin.leveltools;

import static me.byteful.plugin.leveltools.util.Text.colorize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import me.byteful.plugin.leveltools.api.AnvilCombineMode;
import me.byteful.plugin.leveltools.listeners.AnvilListener;
import me.byteful.plugin.leveltools.listeners.BlockEventListener;
import me.byteful.plugin.leveltools.listeners.EntityEventListener;
import me.byteful.plugin.leveltools.util.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.crunch.CompiledExpression;
import redempt.crunch.Crunch;
import redempt.redlib.RedLib;
import redempt.redlib.blockdata.BlockDataManager;
import redempt.redlib.misc.Task;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public final class LevelToolsPlugin extends JavaPlugin {
  private static LevelToolsPlugin instance;

  private BlockDataManager blockDataManager;
  private BukkitCommandHandler commandManager;
  private AnvilCombineMode anvilCombineMode;
  private UpdateChecker updateChecker;
  private CompiledExpression levelXpFormula;
  private Metrics metrics;

  public static LevelToolsPlugin getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    sendStartupBanner();
    instance = this;
    updateChecker = new UpdateChecker(this);

    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }

    final Path blocksFile = getDataFolder().toPath().resolve("player_placed_blocks.db");

    if (!Files.exists(blocksFile)) {
      try {
        blocksFile.toFile().createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    blockDataManager = BlockDataManager.createSQLite(this, blocksFile, true, true);
    blockDataManager.migrate();
    getLogger().info("Loaded BlockDataManager...");

    saveDefaultConfig();
    getConfig().options().copyDefaults(true);
    setAnvilCombineMode();
    setLevelXpFormula();
    getLogger().info("Loaded configuration...");

    if (getConfig().getBoolean("update.start")) {
      updateChecker.check();
    }

    if (getConfig().getBoolean("update.periodically")) {
      final long delay = 20L * TimeUnit.DAYS.toSeconds(1);
      Task.syncRepeating(() -> updateChecker.check(), delay, delay);
    }

    registerListeners();
    getLogger().info("Registered listeners...");

    commandManager = BukkitCommandHandler.create(this);
    commandManager.setHelpWriter(
        (command, actor) ->
            String.format(
                "&7- &b/%s %s&7: &e%s",
                command.getPath().toRealString(), command.getUsage(), command.getDescription()));
    commandManager.register(new LevelToolsCommand());
    commandManager.registerBrigadier();
    getLogger().info("Registered commands...");

    if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      new LevelToolsPlaceholders().register();
    }

    metrics = new Metrics(this, 21451);
    getLogger().info("Successfully started " + getDescription().getFullName() + "!");
  }

  @Override
  public void onDisable() {
    if (blockDataManager != null) {
      blockDataManager.saveAndClose();
      blockDataManager = null;
    }

    if (metrics != null) {
      metrics.shutdown();
    }

    instance = null;

    getLogger().info("Successfully stopped " + getDescription().getFullName() + ".");
  }

  private void sendStartupBanner() {
    Bukkit.getConsoleSender().sendMessage(colorize(" &b         _____"));
    Bukkit.getConsoleSender().sendMessage(colorize(" &d|          &b|     &8Created by &2byteful"));
    Bukkit.getConsoleSender()
        .sendMessage(
            colorize(
                String.format(
                    " &d|          &b|     &8Running &6%s &8on &6MC %s",
                    getDescription().getFullName(), RedLib.getServerVersion())));
    Bukkit.getConsoleSender()
        .sendMessage(
            colorize(
                " &d|_____     &b|     &8Join &9&nhttps://discord.gg/G8BDgqsuyw&8 for support!"));
    Bukkit.getConsoleSender().sendMessage("");
  }

  private void registerListeners() {
    final PluginManager pm = Bukkit.getPluginManager();
    pm.registerEvents(new BlockEventListener(), this);
    pm.registerEvents(new EntityEventListener(), this);
    pm.registerEvents(new AnvilListener(), this);
  }

  public void setAnvilCombineMode() {
    anvilCombineMode =
        AnvilCombineMode.fromName(Objects.requireNonNull(getConfig().getString("anvil_combine")));
  }

  public void setLevelXpFormula() {
    levelXpFormula =
        Crunch.compileExpression(
            getConfig().getString("level_xp_formula").replace("{current_level}", "$1"));
  }

  public BlockDataManager getBlockDataManager() {
    return blockDataManager;
  }

  public AnvilCombineMode getAnvilCombineMode() {
    return anvilCombineMode;
  }

  public CompiledExpression getLevelXpFormula() {
    return levelXpFormula;
  }

  public BukkitCommandHandler getCommandManager() {
    return commandManager;
  }
}
