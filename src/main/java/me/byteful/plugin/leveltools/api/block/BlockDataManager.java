package me.byteful.plugin.leveltools.api.block;


import java.io.Closeable;

public interface BlockDataManager extends Closeable {
  boolean isPlacedBlock(BlockPosition pos);

  void addPlacedBlock(BlockPosition pos);

  void removePlacedBlock(BlockPosition pos);

  void load();
}
