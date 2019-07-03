package com.solozobov.andrei.utils;

import com.solozobov.andrei.RemindException;

/**
 * solozobov on 12/01/2019
 */
public class Assert {
  public static void isTrue(boolean value, String errorMessage, Object ... errorArguments) {
    if (!value) {
      throw new RemindException(String.format(errorMessage, errorArguments));
    }
  }
}
