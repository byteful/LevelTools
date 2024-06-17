package me.byteful.plugin.leveltools.util;

import java.util.*;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import redempt.redlib.misc.FormatUtils;

public final class Text {
  @NotNull
  public static String decolorize(@NotNull String string) {
    return colorize(string).replace("" + ChatColor.COLOR_CHAR, "&");
  }

  @NotNull
  public static String colorize(String input) {
    return FormatUtils.color(input);
  }

  // From Apache lang library
  public static String[] substringsBetween(String str, String open, String close) {
    int strLen = str.length();
    int closeLen = close.length();
    int openLen = open.length();
    List<String> list = new ArrayList();

    int end;
    for (int pos = 0; pos < strLen - closeLen; pos = end + closeLen) {
      int start = str.indexOf(open, pos);
      if (start < 0) {
        break;
      }

      start += openLen;
      end = str.indexOf(close, start);
      if (end < 0) {
        break;
      }

      list.add(str.substring(start, end));
    }

    return list.isEmpty() ? null : list.toArray(new String[0]);
  }
}
