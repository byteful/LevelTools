package me.byteful.plugin.leveltools;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

/**
 * Pulled utility out of lucko's helper library. Since this is pretty much all that was used from
 * helper, I removed the lib to minimize the size of LevelTools' jar.
 */
public final class Text {
  public static String colorize(@NotNull String string) {
    return ChatColor.translateAlternateColorCodes('&', string);
  }
}
