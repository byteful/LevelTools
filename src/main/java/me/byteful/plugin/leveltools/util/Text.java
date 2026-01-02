package me.byteful.plugin.leveltools.util;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Text {
  private static final Set<Character> COLOR_CODES = new HashSet<>();
  static {
    for (char c : "0123456789abcdefklmnor".toCharArray()) {
      COLOR_CODES.add(c);
    }
  }

  @NotNull
  public static String decolorize(@NotNull String string) {
    return colorize(string).replace("" + ChatColor.COLOR_CHAR, "&");
  }

  @NotNull
  public static String colorize(String input) {
    StringBuilder builder = new StringBuilder();
    int len = input.length();

    for (int i = 0; i < len; i++) {
      char c = input.charAt(i);
      if (i + 1 < len) {
        char n = input.charAt(i + 1);

        if (c == '\\' && (n == '&' || n == '\\')) {
          builder.append(n);
          i++;
          continue;
        }

        if (c == '&') {
          if (COLOR_CODES.contains(Character.toLowerCase(n))) {
            builder.append(ChatColor.COLOR_CHAR).append(Character.toLowerCase(n));
            i++;
            continue;
          }

          if (n == '#' && i + 7 < len) {
            String hexCode = input.substring(i + 2, i + 8);
            if (hexCode.chars().allMatch(Text::isHexChar)) {
              builder.append(ChatColor.COLOR_CHAR).append('x');
              for (char hc : hexCode.toCharArray())
                builder.append(ChatColor.COLOR_CHAR).append(Character.toLowerCase(hc));
              i += 7;
              continue;
            }
          }
        }
      }
      builder.append(c);
    }

    return builder.toString();
  }

  private static boolean isHexChar(int c) {
    return (c >= '0' && c <= '9') ||
            (c >= 'A' && c <= 'F') ||
            (c >= 'a' && c <= 'f');
  }

  // Code from https://github.com/boxbeam/RedCommands/blob/master/src/redempt/redlib/misc/FormatUtils.java
  private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("0.00"));
  private static final char[] SUFFIXES = {'K', 'M', 'B', 'T', 'Q'};

  public static String formatMoney(double money) {
    int i = -1;
    while (money >= 1000) {
      money /= 1000;
      i++;
    }
    if (i >= 0) {
      return truncateDouble(money) + SUFFIXES[i];
    }
    return truncateDouble(money);
  }

  public static String truncateDouble(double input) {
    return FORMAT.get().format(input);
  }

  // From Apache lang library
  public static String[] substringsBetween(String str, String open, String close) {
    int strLen = str.length();
    int closeLen = close.length();
    int openLen = open.length();
    List<String> list = new ArrayList<>();

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
