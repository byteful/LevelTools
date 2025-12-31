package me.byteful.plugin.leveltools.api.block.impl;

import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.scheduler.ScheduledTask;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SqliteBlockDataManager implements BlockDataManager {
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS placed_blocks (" +
            "world TEXT NOT NULL, " +
            "x INTEGER NOT NULL, " +
            "y INTEGER NOT NULL, " +
            "z INTEGER NOT NULL, " +
            "UNIQUE(world, x, y, z)" +
            ")";

    private static final String INSERT_BLOCK_SQL = "INSERT OR IGNORE INTO placed_blocks (world, x, y, z) VALUES (?, ?, ?, ?)";

    private static final String DELETE_BLOCK_SQL = "DELETE FROM placed_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?";

    private final Set<BlockPosition> cache = ConcurrentHashMap.newKeySet();
    private final Connection connection;
    private final ScheduledTask saveTask;
    private final Scheduler scheduler;

    public SqliteBlockDataManager(Path dbFile, Scheduler scheduler) {
        this.scheduler = scheduler;

        try {
            Class.forName("org.sqlite.JDBC");

            final Properties properties = new Properties();
            properties.setProperty("foreign_keys", "on");
            properties.setProperty("busy_timeout", "1000");

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toString(), properties);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(CREATE_TABLE_SQL);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup SQLite database!", e);
        }

        this.saveTask = scheduler.asyncTimer(this::save, 5 * 20, 5 * 20);
    }

    private void save() {
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement stmt = connection.prepareStatement(INSERT_BLOCK_SQL)) {
                synchronized (cache) {
                    for (BlockPosition pos : cache) {
                        stmt.setString(1, pos.getWorld());
                        stmt.setInt(2, pos.getX());
                        stmt.setInt(3, pos.getY());
                        stmt.setInt(4, pos.getZ());
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isPlacedBlock(BlockPosition pos) {
        return cache.contains(pos);
    }

    @Override
    public void addPlacedBlock(BlockPosition pos) {
        cache.add(pos);
    }

    @Override
    public void removePlacedBlock(BlockPosition pos) {
        cache.remove(pos);

        scheduler.asyncDelayed(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(DELETE_BLOCK_SQL)) {
                stmt.setString(1, pos.getWorld());
                stmt.setInt(2, pos.getX());
                stmt.setInt(3, pos.getY());
                stmt.setInt(4, pos.getZ());
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, 0);
    }

    @Override
    public void load() {
        cache.clear();
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT world, x, y, z FROM placed_blocks")) {
                while (rs.next()) {
                    cache.add(new BlockPosition(
                            rs.getString("world"),
                            rs.getInt("x"),
                            rs.getInt("y"),
                            rs.getInt("z")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        saveTask.stop();
        save(); // Final save before closing
        cache.clear();

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}