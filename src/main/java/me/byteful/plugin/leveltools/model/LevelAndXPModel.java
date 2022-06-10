package me.byteful.plugin.leveltools.model;

import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.util.LevelToolsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class LevelAndXPModel {
  private final int level;
  private final double xp;

  public LevelAndXPModel(int level, double xp) {
    this.level = level;
    this.xp = LevelToolsUtil.round(xp, 1);
  }

  @NotNull
  public static LevelAndXPModel fromItem(@NotNull LevelToolsItem item) {
    return new LevelAndXPModel(item.getLevel(), item.getXp());
  }

  public int getLevel() {
    return level;
  }

  public double getXp() {
    return xp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LevelAndXPModel that = (LevelAndXPModel) o;
    return level == that.level && Double.compare(that.xp, xp) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(level, xp);
  }

  @Override
  public String toString() {
    return "LevelAndXPModel{" + "level=" + level + ", xp=" + xp + '}';
  }
}
