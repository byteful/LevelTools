package me.byteful.plugin.leveltools.api.block.impl;

import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.scheduler.ScheduledTask;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;
import me.byteful.plugin.leveltools.util.Text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FileBlockDataManager implements BlockDataManager {
    private static final int MAX_CACHE_SIZE = 1_000_000;
    private final Set<BlockPosition> cache = ConcurrentHashMap.newKeySet();
    private final Path file;
    private final ScheduledTask saveTask;

    public FileBlockDataManager(Path file, Scheduler scheduler) {
        this.file = file;
        this.saveTask = scheduler.asyncTimer(this::save, 5 * 20, 5 * 20);
    }

    private void save() {
        final Set<String> lines;
        synchronized (cache) {
            lines = cache.stream()
                    .map(x -> String.format("{%s}{%s}{%s}%s", x.getX(), x.getY(), x.getZ(), x.getWorld()))
                    .collect(Collectors.toSet());
        }

        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPlacedBlock(BlockPosition pos) {
        return cache.contains(pos);
    }

    @Override
    public void addPlacedBlock(BlockPosition pos) {
        if (cache.size() >= MAX_CACHE_SIZE) return;
        cache.add(pos);
    }

    @Override
    public void removePlacedBlock(BlockPosition pos) {
        cache.remove(pos);
    }

    @Override
    public void load() {
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                final String[] data = Text.substringsBetween(line, "{", "}");
                if (data == null || data.length != 3) continue;

                try {
                    final int x = Integer.parseInt(data[0]);
                    final int y = Integer.parseInt(data[1]);
                    final int z = Integer.parseInt(data[2]);
                    final String world = line.substring(String.format("{%s}{%s}{%s}", x, y, z).length());

                    cache.add(new BlockPosition(world, x, y, z));
                } catch (Exception ignored) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        saveTask.stop();
        save();
        cache.clear();
    }
}
