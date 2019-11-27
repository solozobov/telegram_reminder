package com.solozobov.andrei.db;

import com.solozobov.andrei.DomainTestCase;
import com.solozobov.andrei.db.dto.NotificationDto;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static com.solozobov.andrei.utils.Factory.list;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

/**
 * solozobov on 26.11.2019
 */
public class NotificationRepositoryTest extends DomainTestCase {

  @Test
  public void createAndGet() {
    final long chatId = rUniqueLong();
    final int messageId = rUniqueInt();
    final LocalDateTime time = LocalDateTime.now(UTC).minusHours(1);
    final long id = notificationRepository.create(chatId, messageId, time);
    check(id, chatId, messageId, time, null);
  }

  @Test
  public void listExpired() {
    final long chatId1 = rUniqueLong();
    final long chatId2 = rUniqueLong();
    final int messageId1 = rUniqueInt();
    final int messageId2 = rUniqueInt();
    final LocalDateTime now = LocalDateTime.now(UTC);
    final LocalDateTime expectedToExpired = now.minusHours(1);
    final LocalDateTime future = now.plusHours(1);
    final long id = notificationRepository.create(chatId1, messageId1, expectedToExpired);
    notificationRepository.create(chatId2, messageId2, future);

    final List<NotificationDto> actualExpired = notificationRepository.listExpired();
    assertEquals(1, actualExpired.size());
    check(actualExpired.get(0), id, chatId1, messageId1, expectedToExpired, null);
  }

  @Test
  public void update() {
    final long chatId = rUniqueLong();
    final int messageId = rUniqueInt();
    final LocalDateTime time = LocalDateTime.now();
    final long id = notificationRepository.create(chatId, messageId, time);
    check(id, chatId, messageId, time, null);

    final int newRepeatIntervalMinutes = 42;
    notificationRepository.update(chatId, messageId, time, newRepeatIntervalMinutes);
    check(id, chatId, messageId, time, newRepeatIntervalMinutes);
  }

  @Test
  public void updateRepeated() {
    final long chatId = rUniqueLong();
    final int messageId = rUniqueInt();
    final LocalDateTime time = LocalDateTime.now();
    final long id = notificationRepository.create(chatId, messageId, time);

    final int newRepeatIntervalMinutes = 42;
    notificationRepository.update(chatId, messageId, time, newRepeatIntervalMinutes);
    notificationRepository.updateRepeated(singletonList(notificationRepository.get(chatId, messageId)));
    check(id, chatId, messageId, time.plusMinutes(newRepeatIntervalMinutes), newRepeatIntervalMinutes);
  }

  @Test
  public void deleteNotRepeated() {
    final long chatId1 = rUniqueLong();
    final long chatId2 = rUniqueLong();
    final long chatId3 = rUniqueLong();
    final long chatId4 = rUniqueLong();
    final int messageId1 = rUniqueInt();
    final int messageId2 = rUniqueInt();
    final int messageId3 = rUniqueInt();
    final int messageId4 = rUniqueInt();
    final LocalDateTime time = LocalDateTime.now();
    final long id1 = notificationRepository.create(chatId1, messageId1, time);
    final long id2 = notificationRepository.create(chatId2, messageId2, time);
                     notificationRepository.create(chatId3, messageId3, time);
    final long id4 = notificationRepository.create(chatId4, messageId4, time);

    final int repeatIntervalMinutes = 42;
    notificationRepository.update(chatId2, messageId2, time, repeatIntervalMinutes);
    notificationRepository.update(chatId4, messageId4, time, repeatIntervalMinutes);

    notificationRepository.deleteNotRepeated(list(notificationRepository.get(chatId3, messageId3), notificationRepository.get(chatId4, messageId4)));
    check(id1, chatId1, messageId1, time, null);
    check(id2, chatId2, messageId2, time, repeatIntervalMinutes);
    assertNull(notificationRepository.get(chatId3, messageId3));
    check(id4, chatId4, messageId4, time, repeatIntervalMinutes);
  }
}
