package com.solozobov.andrei.db;

import com.solozobov.andrei.utils.Converter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.solozobov.andrei.utils.Assert.notNull;

/**
 * solozobov on 05.11.2019
 */
@SuppressWarnings("WeakerAccess")
public class SettingKey<T> {
  private static final Map<String, SettingKey> key2settingKey = new HashMap<>();

  public static @NotNull SettingKey get(@NotNull String key) {
    return notNull(key2settingKey.get(key), "No SettingKey with name '" + key + "'");
  }

  public final String key;
  public final T defaultValue;
  public final Converter<T, String> valueConverter;

  public static SettingKey<Integer> create(@NotNull String key, @Nullable Integer defaultValue) {
    return new SettingKey<>(key, defaultValue, nullableConverter("", value -> value + "", Integer::parseInt));
  }

  public static SettingKey<Long> create(@NotNull String key, @Nullable Long defaultValue) {
    return new SettingKey<>(key, defaultValue, nullableConverter("", value -> value  + "", Long::parseLong));
  }

  public static SettingKey<String> create(@NotNull String key, @Nullable String defaultValue) {
    return new SettingKey<>(key, defaultValue, nullableConverter("¤", value -> value, value -> value));
  }

  public static SettingKey<Boolean> create(@NotNull String key, @Nullable Boolean defaultValue) {
    return new SettingKey<>(key, defaultValue, nullableConverter("", value -> value ? "T" : "F", value -> value.equals("T")));
  }

  private static <FROM,TO> Converter<@Nullable FROM, @NotNull TO> nullableConverter(
      @NotNull TO nullValue,
      Function<@NotNull FROM, TO> forward,
      Function<TO, @NotNull FROM> backward
  ) {
    return new Converter<FROM, TO>() {
      @Override
      public TO convert(FROM from) {
        return from == null ? nullValue : forward.apply(from);
      }

      @Override
      public FROM reverse(TO to) {
        return to.equals(nullValue) ? null : backward.apply(to);
      }
    };
  }

  protected SettingKey(
      @NotNull String key,
      T defaultValue,
      @NotNull Converter<T, String> valueConverter
  ) {
    this.key = key;
    this.defaultValue = defaultValue;
    this.valueConverter = valueConverter;
    key2settingKey.put(key, this);
  }

  @Override
  public String toString() {
    return "'" + key + "'";
  }
}
