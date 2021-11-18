package me.byteful.plugin.leveltools;

import me.byteful.plugin.leveltools.listeners.BlockEventListener;
import me.byteful.plugin.leveltools.listeners.EntityEventListener;
import me.lucko.helper.Commands;
import me.lucko.helper.command.CommandInterruptException;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import redempt.redlib.blockdata.BlockDataManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class LevelToolsPlugin extends ExtendedJavaPlugin {
  private static LevelToolsPlugin instance;

  private BlockDataManager blockDataManager;

  public static LevelToolsPlugin getInstance() {
    return instance;
  }

  @Override
  protected void enable() {
    instance = this;

    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
    }

    // Support older file names.
    final Path blocksFile = getDataFolder().toPath().resolve("player_placed_blocks.db");
    final Path oldFile = getDataFolder().toPath().resolve("blocks.db");
    if(Files.exists(oldFile)) {
      if(Files.exists(blocksFile)) {
        getLogger().warning("Found old 'blocks.db' file, but ignored it because a newer 'player_placed_blocks.db' file exists!");
      } else {
        try {
          Files.move(oldFile, blocksFile, StandardCopyOption.COPY_ATTRIBUTES);
          getLogger()
              .warning("Found old 'blocks.db' file... Moved to newer 'player_placed_blocks.db' file.");
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

    blockDataManager = new BlockDataManager(blocksFile);
    blockDataManager.setAutoSave(true);
    getLogger().info("Loaded BlockDataManager...");

    saveDefaultConfig();
    getLogger().info("Loaded configuration...");

    registerListeners();
    getLogger().info("Registered listeners...");

    registerReloadCommand();
    getLogger().info("Registered commands...");

    getLogger().info("Successfully started " + getDescription().getFullName() + "!");
  }

  @Override
  protected void disable() {
    if (blockDataManager != null) {
      blockDataManager.saveAndClose();
      blockDataManager = null;
    }

    instance = null;

    getLogger().info("Successfully stopped " + getDescription().getFullName() + ".");
  }

  private void registerListeners() {
    registerListener(new BlockEventListener());
    registerListener(new EntityEventListener());
  }

  private void registerReloadCommand() {
    Commands.create()
        .description("Reloads the configuration for LevelTools.")
        .handler(
            c -> {
              if (!c.sender().hasPermission("leveltools.admin")) {
                throw new CommandInterruptException(
                    getConfig().getString("messages.no_permission"));
              }

              reloadConfig();

              c.reply(getConfig().getString("messages.successful_reload"));
            })
        .register("leveltools-reload", "lt-reload");
  }

  public BlockDataManager getBlockDataManager() {
    return blockDataManager;
  }
}
