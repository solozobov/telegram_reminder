package com.solozobov.andrei.utils;

import com.google.common.annotations.VisibleForTesting;
import com.solozobov.andrei.RememberException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * solozobov on 12/01/2019
 */
public class State {
  private static final char[] ALPHABET = "\u2060\u2061\u2062\u2063\u2064\u2065\u2066\u2067\u2068\u2069\u206A\u206B\u206C\u206D\u206E\u206F".toCharArray();
  private static final String SEPARATOR = "\u180E";
  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
  private static final String TYPE_KEY = "type";
  private static final String DATE_KEY = "date";

  public enum NotificationType {
    REPEATED("repeat"),
    ONCE("once");

    public final String key;

    NotificationType(String key) {
      this.key = key;
    }

    public static NotificationType getByKey(String key) {
      for (NotificationType notificationType : NotificationType.values()) {
        if (notificationType.key.equals(key)) {
          return notificationType;
        }
      }
      throw new RememberException("No NotificationType with key '" + key + "'");
    }
  }

  public NotificationType notificationType;
  public LocalDate date;

  public static State deserialize(String message) {
    final int index = message.indexOf(SEPARATOR);
    if (index < 0) {
      return new State();
    } else {
      return fromJson(new JSONObject(decode(message.substring(0, index))));
    }
  }

  private static State fromJson(JSONObject json) {
    final State state = new State();
    state.notificationType = json.has(TYPE_KEY) ? NotificationType.getByKey(json.getString(TYPE_KEY)) : null;
    state.date = json.has(DATE_KEY) ? LocalDate.parse(json.getString(DATE_KEY), dateFormatter) : null;
    return state;
  }

  public String serialize() {
    return encode(this.toJson().toString()) + SEPARATOR;
  }

  private JSONObject toJson() {
    final JSONObject result = new JSONObject();
    if (notificationType != null) {
      result.put(TYPE_KEY, notificationType.key);
    }
    if (date != null) {
      result.put(DATE_KEY, date.format(dateFormatter));
    }
    return result;
  }

  @VisibleForTesting static String decode(String message) {
    Assert.isTrue(message.length() % 2 == 0, "Message '%s' length is %s, it can't be odd", message, message.length());
    final StringBuilder result = new StringBuilder(message.length() / 2);
    final char[] chars = message.toCharArray();
    for (int i = 0; i < chars.length; i += 2) {
      result.append((char) ((((int) chars[i] - ALPHABET[0]) << 4) + ((int) chars[i + 1] - ALPHABET[0])));
    }
    return result.toString();
  }

  @VisibleForTesting static String encode(String message) {
    final StringBuilder result = new StringBuilder(message.length() * 2);
    for (char ch : message.toCharArray()) {
      Assert.isTrue((int) ch <= 255, "Can't encode symbol '%s' out of [0;255]" , ch);
      result.append(ALPHABET[((int) ch & 0b11110000) >> 4]).append(ALPHABET[(int) ch & 0b00001111]);
    }
    return result.toString();
  }
}
