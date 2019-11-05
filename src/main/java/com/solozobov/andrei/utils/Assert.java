package com.solozobov.andrei.utils;

import com.solozobov.andrei.RememberException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * solozobov on 12/01/2019
 */
public class Assert {

  @NotNull
  public static <T> T notNull(@Nullable T val, @NotNull String errorMessageFormat, Object... errorArguments) {
    if (val == null) {
      throw new RememberException(errorMessageFormat, errorArguments);
    }
    return val;
  }

  public static void isTrue(boolean value, String errorMessage, Object ... errorArguments) {
    if (!value) {
      throw new RememberException(String.format(errorMessage, errorArguments));
    }
  }
}
