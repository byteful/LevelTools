package me.byteful.plugin.leveltools.api;

import me.byteful.plugin.leveltools.model.LevelAndXPModel;
import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;

public enum AnvilCombineMode {
  HIGHER_OF_BOTH((item1, item2) -> {
    final int level1 = item1.getLevel();
    final int level2 = item2.getLevel();

    final double xp1 = item1.getXp();
    final double xp2 = item2.getXp();

    int level;
    double xp;

    if (level1 == level2) {
      level = level1;
      xp = Math.max(xp1, xp2);
    } else {
      level = Math.max(level1, level2);
      if (level == level1) {
        xp = xp1;
      } else {
        xp = xp2;
      }
    }

    return new LevelAndXPModel(level, xp);
  }),
  LOWER_OF_BOTH((item1, item2) -> {
    final int level1 = item1.getLevel();
    final int level2 = item2.getLevel();

    final double xp1 = item1.getXp();
    final double xp2 = item2.getXp();

    int level;
    double xp;

    if (level1 == level2) {
      level = level1;
      xp = Math.min(xp1, xp2);
    } else {
      level = Math.min(level1, level2);
      if (level == level1) {
        xp = xp1;
      } else {
        xp = xp2;
      }
    }

    return new LevelAndXPModel(level, xp);
  }),
  ADD_BOTH((item1, item2) -> {
    final int level1 = item1.getLevel();
    final int level2 = item2.getLevel();

    final double xp1 = item1.getXp();
    final double xp2 = item2.getXp();

    return new LevelAndXPModel(level1 + level2, xp1 + xp2);
  });

  @NotNull
  private final BinaryOperator<LevelAndXPModel> handler;

  AnvilCombineMode(@NotNull BinaryOperator<LevelAndXPModel> handler) {
    this.handler = handler;
  }

  @NotNull
  public static AnvilCombineMode fromName(@NotNull String name) {
    for (AnvilCombineMode value : values()) {
      if (value.name().equalsIgnoreCase(name.replace(" ", "_"))) {
        return value;
      }
    }

    return ADD_BOTH;
  }

  @NotNull
  public BinaryOperator<LevelAndXPModel> getHandler() {
    return handler;
  }

  @Override
  public String toString() {
    return "AnvilCombineMode{" +
        "handler=" + handler +
        '}';
  }
}
