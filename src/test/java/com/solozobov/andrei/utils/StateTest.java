package com.solozobov.andrei.utils;

import com.solozobov.andrei.RememberException;
import org.junit.Test;

import java.time.LocalDate;

import static com.solozobov.andrei.utils.State.decode;
import static com.solozobov.andrei.utils.State.deserialize;
import static com.solozobov.andrei.utils.State.encode;
import static org.junit.Assert.*;

/**
 * solozobov on 12/01/2019
 */
public class StateTest {

  @Test
  public void test_serializeDeserialize() {
    final State expected = deserialize("");
    assertNull(expected.notificationType);
    assertNull(expected.date);

    final State actual = deserialize(expected.serialize());
    assertNull(actual.notificationType);
    assertNull(actual.date);

    expected.notificationType = State.NotificationType.ONCE;
    final State actual2 = deserialize(expected.serialize());
    assertEquals(expected.notificationType, actual2.notificationType);
    assertNull(actual2.date);

    expected.date = LocalDate.now();
    final State actual3 = deserialize(expected.serialize());
    assertEquals(expected.notificationType, actual3.notificationType);
    assertEquals(expected.date, actual3.date);
  }

  @Test
  public void test_encodeDecode() {
    assertEquals("", decode(encode("")));
    assertEquals("abacabadaba", decode(encode("abacabadaba")));
    assertEquals("{\"test\":\"json\"}", decode(encode("{\"test\":\"json\"}")));
  }

  @Test(expected = RememberException.class)
  public void test_encodeDecodeInvalidSymbol() {
    assertEquals("{\"ключ\":\"значение\"}", decode(encode("{\"ключ\":\"значение\"}")));
  }
}
