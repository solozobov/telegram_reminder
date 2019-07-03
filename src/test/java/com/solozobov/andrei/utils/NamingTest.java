package com.solozobov.andrei.utils;

import org.junit.Test;

import static com.solozobov.andrei.utils.Naming.daysAccusative;
import static com.solozobov.andrei.utils.Naming.hoursAccusative;
import static com.solozobov.andrei.utils.Naming.minutesAccusative;
import static org.junit.Assert.*;

/**
 * solozobov on 20/01/2019
 */
public class NamingTest {

  @Test
  public void test_daysAccusative() {
    for (long i = 0; i < 100; i++) {
      System.out.println(daysAccusative(i));
    }
  }

  @Test
  public void test_hoursAccusative() {
    for (long i = 0; i < 100; i++) {
      System.out.println(hoursAccusative(i));
    }
  }

  @Test
  public void test_minutesAccusative() {
    for (long i = 0; i < 100; i++) {
      System.out.println(minutesAccusative(i));
    }
  }
}
