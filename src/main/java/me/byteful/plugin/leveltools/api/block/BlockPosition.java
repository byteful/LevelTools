package me.byteful.plugin.leveltools.api.block;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public final class BlockPosition {
  private final String world;
  private final int x, y, z;

  public BlockPosition(String world, int x, int y, int z) {
    this.world = world;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public String getWorld() {
    return world;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getZ() {
    return z;
  }

  public static BlockPosition fromBukkit(Block block) {
    final Location l = block.getLocation();

    return new BlockPosition(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
  }

  public Block toBukkit() {
    final World bw = Bukkit.getWorld(world);
    if (bw == null) return null;

    return new Location(bw, x, y, z).getBlock();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BlockPosition that = (BlockPosition) o;
    return getX() == that.getX() && getY() == that.getY() && getZ() == that.getZ() && Objects.equals(getWorld(), that.getWorld());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getWorld(), getX(), getY(), getZ());
  }

  @Override
  public String toString() {
    return "BlockPosition{" + "world='" + world + '\'' + ", x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
