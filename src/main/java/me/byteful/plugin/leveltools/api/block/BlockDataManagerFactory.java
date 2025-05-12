package me.byteful.plugin.leveltools.api.block;

import me.byteful.plugin.leveltools.api.block.impl.FileBlockDataManager;
import me.byteful.plugin.leveltools.api.block.impl.SqliteBlockDataManager;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import org.bukkit.configuration.ConfigurationSection;

import java.nio.file.Path;
import java.sql.SQLException;

public class BlockDataManagerFactory {
    public static BlockDataManager createBlockDataManager(Path dataFolder, ConfigurationSection config, Scheduler scheduler) {
        String storageType = config.getString("block_data_storage.type", "LEGACY_TEXT");

        switch (storageType.toUpperCase()) {
            case "SQLITE":
                return new SqliteBlockDataManager(dataFolder.resolve("placed_blocks.db"), scheduler);
            case "LEGACY_TEXT":
            default:
                return new FileBlockDataManager(dataFolder.resolve("placed_blocks.txt"), scheduler);
        }
    }
}