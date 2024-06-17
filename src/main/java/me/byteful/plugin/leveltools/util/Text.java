package me.byteful.plugin.leveltools.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

// Borrowed from RedLib (https://github.com/boxbeam/RedCommands/blob/master/src/redempt/redlib/misc/FormatUtils.java)
public final class Text {
  @NotNull
  public static String decolorize(@NotNull String string) {
    return colorize(string).replace("" + ChatColor.COLOR_CHAR, "&");
  }

  private static Set<Character> colorChars = "4c6e2ab319d5f780rlonmk".chars().mapToObj(i -> (char) i).collect(Collectors.toSet());

  @NotNull
  // Author: boxbeam
  public static String colorize(String input) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      if (i + 1 >= input.length()) {
        builder.append(c);
        continue;
      }
      char n = input.charAt(i + 1);
      if (c == '\\' && (n == '&' || n == '\\')) {
        i++;
        builder.append(n);
        continue;
      }
      if (c != '&') {
        builder.append(c);
        continue;
      }
      if (colorChars.contains(n)) {
        builder.append(ChatColor.COLOR_CHAR);
        continue;
      }
      if (n == '#' && i + 7 <= input.length()) {
        String hexCode = input.substring(i + 2, i + 8).toUpperCase(Locale.ROOT);
        if (hexCode.chars().allMatch(ch -> (ch <= '9' && ch >= '0') || (ch <= 'F' && ch >= 'A'))) {
          hexCode = Arrays.stream(hexCode.split("")).map(s -> ChatColor.COLOR_CHAR + s).collect(Collectors.joining());
          builder.append(ChatColor.COLOR_CHAR).append("x").append(hexCode);
          i += 7;
          continue;
        }
      }
      builder.append(c);
    }
    return builder.toString();
  }
}
