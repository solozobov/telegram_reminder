package com.solozobov.andrei.db;

import com.solozobov.andrei.db.dto.NotificationDto;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.Collection;
import java.util.List;

import static com.solozobov.andrei.utils.Factory.map;
import static org.jooq.DatePart.MINUTE;
import static org.jooq.db.Tables.NOTIFICATIONS;
import static org.jooq.impl.DSL.*;

/**
 * solozobov on 13/01/2019
 */
@Repository
public class NotificationRepository {

  private final DSLContext db;

  public void create(long chatId, int messageId, LocalDateTime dateTimeUtc) {
    db.insertInto(NOTIFICATIONS, NOTIFICATIONS.CHAT_ID, NOTIFICATIONS.MESSAGE_ID, NOTIFICATIONS.TIMESTAMP_UTC, NOTIFICATIONS.FORESTALL_MINUTES)
      .values(chatId, messageId, dateTimeUtc, 0)
      .execute();
  }

  public List<NotificationDto> listExpiredNotifications() {
    return db.select(NOTIFICATIONS.ID, NOTIFICATIONS.CHAT_ID, NOTIFICATIONS.MESSAGE_ID)
      .from(NOTIFICATIONS)
      .where(localDateTimeAdd(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")), NOTIFICATIONS.FORESTALL_MINUTES, MINUTE).ge(NOTIFICATIONS.TIMESTAMP_UTC))
      .fetchInto(NotificationDto.class);
  }

  public void delete(Collection<NotificationDto> notifications) {
    db.delete(NOTIFICATIONS)
      .where(NOTIFICATIONS.ID.in(map(notifications, n -> n.notificationId)))
      .execute();
  }

  public void update(long chatId, int messageId, LocalDateTime dateTimeUtc, int repeatIntervalMinutes) {
    db.update(NOTIFICATIONS)
      .set(NOTIFICATIONS.TIMESTAMP_UTC, dateTimeUtc)
      .set(NOTIFICATIONS.FORESTALL_MINUTES, 0)
      .set(NOTIFICATIONS.REPEAT_MINUTES, repeatIntervalMinutes)
      .where(NOTIFICATIONS.CHAT_ID.eq(chatId))
      .and(NOTIFICATIONS.MESSAGE_ID.eq(messageId))
      .execute();
  }

  @Autowired
  public NotificationRepository(DSLContext db) {
    this.db = db;
  }
}
