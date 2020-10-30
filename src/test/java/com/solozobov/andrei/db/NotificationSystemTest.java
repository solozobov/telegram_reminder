package com.solozobov.andrei.db;

import com.solozobov.andrei.DomainTestCase;
import com.solozobov.andrei.utils.Pair;
import org.jooq.db.tables.records.NotificationsRecord;
import org.junit.Test;

import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.assertNull;

/**
 * solozobov on 28.11.2019
 */
public class NotificationSystemTest extends DomainTestCase {
  @Test
  public void run() {
    final long chatId1 = rUniqueLong();
    final long chatId2 = rUniqueLong();
    final long chatId3 = rUniqueLong();
    final int messageId1 = rUniqueInt();
    final int messageId2 = rUniqueInt();
    final int messageId3 = rUniqueInt();
    final LocalDateTime now = LocalDateTime.now(UTC);
    final LocalDateTime expired = now.minusHours(1);
    final LocalDateTime future = now.plusHours(1);

    // expired and not repeated ==> must be deleted
    notificationRepository.create(chatId1, messageId1, expired);

    // expired and repeated ==> must be rescheduled
    final NotificationsRecord notification2 = notificationRepository.create(chatId2, messageId2, expired);
    final int repeatIntervalMinutes = 90;
    notificationRepository.update(chatId2, messageId2, expired, repeatIntervalMinutes);

    // not expired ==> untouched
    final NotificationsRecord notification3 = notificationRepository.create(chatId3, messageId3, future);

    telegramBot.reset();
    notificationSystem.loop();
    telegramBot.assertForwarded(Pair.of(chatId1, messageId1), Pair.of(chatId2, messageId2));

    assertNull(notificationRepository.get(chatId1, messageId1));
    check(notificationRepository.get(chatId2, messageId2), notification2.getId(), chatId2, messageId2, expired.plusMinutes(repeatIntervalMinutes), repeatIntervalMinutes);
    check(notification3, chatId3, messageId3, future, null);
  }
}
