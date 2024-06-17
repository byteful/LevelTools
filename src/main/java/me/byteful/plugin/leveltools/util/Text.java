package me.byteful.plugin.leveltools.util;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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

  // From Apache lang library
  public static String[] substringsBetween(String str, String open, String close) {
    int strLen = str.length();
    if (strLen == 0) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    } else {
      int closeLen = close.length();
      int openLen = open.length();
      List<String> list = new ArrayList();

      int end;
      for(int pos = 0; pos < strLen - closeLen; pos = end + closeLen) {
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

      return list.isEmpty() ? null : (String[])list.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
  }
}
