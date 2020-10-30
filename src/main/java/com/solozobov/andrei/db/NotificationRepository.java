package com.solozobov.andrei.db;

import com.solozobov.andrei.db.dto.NotificationDto;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.db.tables.records.NotificationsRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.*;
import java.util.Collection;
import java.util.List;

import static com.solozobov.andrei.utils.Factory.map;
import static java.util.stream.Collectors.toList;
import static org.jooq.DatePart.MINUTE;
import static org.jooq.db.Tables.NOTIFICATIONS;
import static org.jooq.impl.DSL.*;

/**
 * solozobov on 13/01/2019
 */
@Repository
public class NotificationRepository {

  private final DSLContext db;

  public NotificationsRecord create(long chatId, int messageId, LocalDateTime dateTimeUtc) {
    return db.insertInto(NOTIFICATIONS, NOTIFICATIONS.CHAT_ID, NOTIFICATIONS.MESSAGE_ID, NOTIFICATIONS.TIMESTAMP_UTC, NOTIFICATIONS.FORESTALL_MINUTES)
      .values(chatId, messageId, dateTimeUtc, 0)
      .returning()
      .fetchOne();
  }

  public @Nullable NotificationDto get(long chatId, int messageId) {
    return db.select()
      .from(NOTIFICATIONS)
      .where(NOTIFICATIONS.CHAT_ID.eq(chatId)).and(NOTIFICATIONS.MESSAGE_ID.eq(messageId))
      .fetchOneInto(NotificationDto.class);
  }

  public List<NotificationDto> listExpired() {
    return db.select()
      .from(NOTIFICATIONS)
      .where(localDateTimeAdd(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")), NOTIFICATIONS.FORESTALL_MINUTES, MINUTE).ge(NOTIFICATIONS.TIMESTAMP_UTC))
      .fetchInto(NotificationDto.class);
  }

  public void updateRepeated(Collection<NotificationDto> notifications) {
    db.update(NOTIFICATIONS)
      .set(NOTIFICATIONS.TIMESTAMP_UTC, localDateTimeAdd(NOTIFICATIONS.TIMESTAMP_UTC, NOTIFICATIONS.REPEAT_MINUTES, MINUTE))
      .where(NOTIFICATIONS.ID.in(notifications.stream().map(n -> n.notificationId).collect(toList())))
      .and(NOTIFICATIONS.REPEAT_MINUTES.isNotNull())
      .execute();
  }

  public void deleteNotRepeated(Collection<NotificationDto> notifications) {
    db.delete(NOTIFICATIONS)
      .where(NOTIFICATIONS.ID.in(map(notifications, n -> n.notificationId)))
      .and(NOTIFICATIONS.REPEAT_MINUTES.isNull())
      .execute();
  }

  public void update(long chatId, int messageId, LocalDateTime dateTimeUtc, @Nullable Integer repeatIntervalMinutes) {
    db.update(NOTIFICATIONS)
      .set(NOTIFICATIONS.TIMESTAMP_UTC, dateTimeUtc)
      .set(NOTIFICATIONS.FORESTALL_MINUTES, 0)
      .set(NOTIFICATIONS.REPEAT_MINUTES, repeatIntervalMinutes)
      .where(NOTIFICATIONS.CHAT_ID.eq(chatId))
      .and(NOTIFICATIONS.MESSAGE_ID.eq(messageId))
      .execute();
  }

  public void delete(Long chatId, int messageId) {
    db.delete(NOTIFICATIONS)
      .where(NOTIFICATIONS.CHAT_ID.eq(chatId))
      .and(NOTIFICATIONS.MESSAGE_ID.eq(messageId))
      .execute();
  }

  @Autowired
  public NotificationRepository(DSLContext db) {
    this.db = db;
  }
}
