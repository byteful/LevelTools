package me.byteful.plugin.leveltools;

import me.byteful.plugin.leveltools.listeners.BlockEventListener;
import me.byteful.plugin.leveltools.listeners.EntityEventListener;
import me.lucko.helper.Commands;
import me.lucko.helper.command.CommandInterruptException;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import redempt.redlib.blockdata.BlockDataManager;

import java.io.File;
import java.io.IOException;

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

    final File blocksFile = new File(getDataFolder(), "blocks.db");
    if (!blocksFile.exists()) {
      try {
        blocksFile.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    blockDataManager = new BlockDataManager(blocksFile.toPath());
    blockDataManager.setAutoSave(true);
    getLogger().info("Loaded BlockDataManager...");

    saveDefaultConfig();
    getLogger().info("Loaded configuration...");

    registerListeners();
    getLogger().info("Registered listeners...");

    registerCommands();
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

  private void registerCommands() {
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
