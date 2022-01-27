package me.byteful.plugin.leveltools;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.PaperCommandManager;
import me.byteful.plugin.leveltools.api.AnvilCombineMode;
import me.byteful.plugin.leveltools.listeners.AnvilListener;
import me.byteful.plugin.leveltools.listeners.BlockEventListener;
import me.byteful.plugin.leveltools.listeners.EntityEventListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import redempt.redlib.blockdata.BlockDataManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class LevelToolsPlugin extends JavaPlugin {
  private static LevelToolsPlugin instance;

  private BlockDataManager blockDataManager;
  private BukkitCommandManager commandManager;
  private AnvilCombineMode anvilCombineMode;

  public static LevelToolsPlugin getInstance() {
    return instance;
  }

  @Override
  public void onEnable() {
    instance = this;

    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }

    // Support older file names.
    final Path blocksFile = getDataFolder().toPath().resolve("player_placed_blocks.db");
    final Path oldFile = getDataFolder().toPath().resolve("blocks.db");
    if (Files.exists(oldFile)) {
      if (Files.exists(blocksFile)) {
        getLogger()
            .warning(
                "Found old 'blocks.db' file, but ignored it because a newer 'player_placed_blocks.db' file exists!");
      } else {
        try {
          Files.move(oldFile, blocksFile, StandardCopyOption.COPY_ATTRIBUTES);
          getLogger()
              .warning(
                  "Found old 'blocks.db' file... Moved to newer 'player_placed_blocks.db' file.");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    if (!Files.exists(blocksFile)) {
      try {
        blocksFile.toFile().createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    blockDataManager = BlockDataManager.createSQLite(this, blocksFile, true, true);
    getLogger().info("Loaded BlockDataManager...");

    saveDefaultConfig();
    getConfig().options().copyDefaults(true).copyHeader(true);
    setAnvilCombineMode();
    getLogger().info("Loaded configuration...");

    registerListeners();
    getLogger().info("Registered listeners...");

    commandManager = new PaperCommandManager(this);
    commandManager.enableUnstableAPI("brigadier");
    commandManager.enableUnstableAPI("help");
    commandManager.registerCommand(new LevelToolsCommand());
    getLogger().info("Registered commands...");

    getLogger().info("Successfully started " + getDescription().getFullName() + "!");
  }

  @Override
  public void onDisable() {
    if (blockDataManager != null) {
      blockDataManager.saveAndClose();
      blockDataManager = null;
    }

    instance = null;

    getLogger().info("Successfully stopped " + getDescription().getFullName() + ".");
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

  public BlockDataManager getBlockDataManager() {
    return blockDataManager;
  }

  public AnvilCombineMode getAnvilCombineMode() {
    return anvilCombineMode;
  }
}
