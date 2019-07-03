package com.solozobov.andrei.utils;

/**
 * solozobov on 14/01/2019
 */
public class Threads {
  public static void sleep(long millis) {
    try {
      java.lang.Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
