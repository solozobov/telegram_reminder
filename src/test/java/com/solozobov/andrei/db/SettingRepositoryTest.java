package com.solozobov.andrei.db;

import com.solozobov.andrei.DomainTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * solozobov on 05.11.2019
 */
public class SettingRepositoryTest extends DomainTestCase {

  @Test
  public void setGetDelete() {
    final String key1 = "key1";
    assertNull(settingRepository.get(key1));

    final String value1 = "value1";
    settingRepository.set(key1, value1);
    assertEquals(value1, settingRepository.get(key1));
    settingRepository.set(key1, value1);
    assertEquals(value1, settingRepository.get(key1));

    final String key2 = "key2";
    final String value2 = "value2";
    settingRepository.set(key2, value2);
    assertEquals(value2, settingRepository.get(key2));

    final String value3 = "value3";
    settingRepository.set(key1, value3);
    assertEquals(value3, settingRepository.get(key1));
    assertEquals(value2, settingRepository.get(key2));

    settingRepository.delete(key1);
    assertNull(settingRepository.get(key1));
    assertEquals(value2, settingRepository.get(key2));

    settingRepository.delete(key1);
    assertNull(settingRepository.get(key1));
    assertEquals(value2, settingRepository.get(key2));
  }
}
