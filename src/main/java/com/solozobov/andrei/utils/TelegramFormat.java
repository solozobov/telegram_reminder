package com.solozobov.andrei.utils;

import org.jetbrains.annotations.NotNull;

/**
 * solozobov on 03.07.2019
 */
// https://core.telegram.org/bots/api#formatting-options
public class TelegramFormat {

  public static String bold(@NotNull String text) {
    return "*" + text + "*";
  }

  public static String italic(@NotNull String text) {
    return "_" + text + "_";
  }

  public static String link(@NotNull String text, @NotNull String url) {
    return "[" + text + "](" + url + ")";
  }

  public static String userLink(@NotNull String text, long userId) {
    return "[" + text + "](tg://user?id=" + userId + ")";
  }

  public static String userLink(@NotNull String text, String login) {
    return "[" + text + "](https://t.me/" + login + ")";
  }

  public static String paragraph(@NotNull String paragraph) {
    return "```" + paragraph + "```";
  }
}
