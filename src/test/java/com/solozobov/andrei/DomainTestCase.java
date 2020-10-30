package com.solozobov.andrei;

import com.solozobov.andrei.db.NotificationRepository;
import com.solozobov.andrei.db.NotificationSystem;
import com.solozobov.andrei.db.SettingRepository;
import com.solozobov.andrei.db.UserRepository;
import com.solozobov.andrei.db.dto.NotificationDto;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.db.tables.records.NotificationsRecord;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * solozobov on 26.11.2019
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource({"classpath:spring/application.properties"})
@ContextConfiguration(locations = "classpath:spring/domain-test.xml", inheritLocations = false)
public abstract class DomainTestCase {
  private static final Logger LOG = LoggerFactory.getLogger(DomainTestCase.class);

  @Autowired protected UserRepository userRepository;
  @Autowired protected NotificationRepository notificationRepository;
  @Autowired protected NotificationSystem notificationSystem;
  @Autowired protected SettingRepository settingRepository;
  @Autowired protected TestTelegramBot telegramBot;

  private static final Random STATIC_RANDOM = new Random();

  private long seed = STATIC_RANDOM.nextLong();
  private Random random = new Random(seed);

  @Rule
  public TestRule writeRandomSeedOnFail = new TestWatcher() {
    @Override
    protected void failed(Throwable e, Description description) {
      LOG.error(String.format("Randomized test '%s.%s' failed on seed '%sL':",
          description.getClassName(), description.getMethodName(), seed), e);
    }
  };

  @SuppressWarnings("unused")
  protected void setSeed(long seed) {
    this.seed = seed;
    this.random = new Random(seed);
  }

  protected Random getRandom() {
    return random;
  }

  private final Set<Long> usedLongs = new HashSet<>();

  protected long rUniqueLong() {
    long candidate;
    do {
      candidate = rLong();
    } while(!usedLongs.add(candidate));

    return candidate;
  }

  protected long rLong() {
    return random.nextLong();
  }

  private final Set<Integer> usedInts = new HashSet<>();

  protected int rUniqueInt() {
    int candidate;
    do {
      candidate = rInt();
    } while(!usedInts.add(candidate));

    return candidate;
  }

  protected int rInt() {
    return random.nextInt();
  }

  private final Set<String> usedStrings = new HashSet<>();

  protected String rUniqString() {
    String result;
    do {
      result = rString(random.nextInt(32) + 3);
    } while (!usedStrings.add(result));
    return result;
  }

  private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-";

  protected String rString() {
    return rString(random.nextInt(32) + 3);
  }

  protected String rString(int length) {
    final char[] result = new char[length];
    for (int i = 0; i < length; i++) {
      result[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
    }
    return String.valueOf(result);
  }

  protected void check(
      NotificationsRecord actual,
      long expectedChatId,
      int expectedMessageId,
      @NotNull LocalDateTime expectedTime,
      @Nullable Integer expectedRepeatIntervalMinutes
  ) {
    check(notificationRepository.get(expectedChatId, expectedMessageId), actual.getId(), expectedChatId, expectedMessageId, expectedTime, expectedRepeatIntervalMinutes);
    assertNotNull(actual);
    assertEquals((Long) expectedChatId, actual.getChatId());
    assertEquals((Integer) expectedMessageId, actual.getMessageId());
    assertEquals(expectedTime, actual.getTimestampUtc());
    assertEquals(expectedRepeatIntervalMinutes, actual.getRepeatMinutes());
  }

  protected void check(
      NotificationDto actual,
      long expectedId,
      long expectedChatId,
      int expectedMessageId,
      @NotNull LocalDateTime expectedTime,
      @Nullable Integer expectedRepeatIntervalMinutes
  ) {
    assertNotNull(actual);
    assertEquals(expectedId, actual.notificationId);
    assertEquals(expectedChatId, actual.chatId);
    assertEquals(expectedMessageId, actual.messageId);
    assertEquals(expectedTime, actual.time);
    assertEquals(expectedRepeatIntervalMinutes, actual.repeatIntervalMinutes);
  }
}
