package com.solozobov.andrei.db;

import com.solozobov.andrei.db.dto.NotificationDto;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.solozobov.andrei.utils.Factory.map;
import static org.jooq.db.Tables.NOTIFICATIONS;
import static org.jooq.impl.DSL.*;

/**
 * solozobov on 13/01/2019
 */
@Repository
public class NotificationRepository {

  private final DSLContext db;

  public void create(long chatId, int messageId, LocalDateTime when) {
    db.insertInto(NOTIFICATIONS, NOTIFICATIONS.CHAT_ID, NOTIFICATIONS.MESSAGE_ID, NOTIFICATIONS.TIME, NOTIFICATIONS.FORESTALL_MINUTES)
      .values(chatId, messageId, when, 0)
      .execute();
  }

  public List<NotificationDto> listExpiredNotifications() {
    return db.select(NOTIFICATIONS.ID, NOTIFICATIONS.CHAT_ID, NOTIFICATIONS.MESSAGE_ID)
      .from(NOTIFICATIONS)
      .where(localDateTimeAdd(currentLocalDateTime(), NOTIFICATIONS.FORESTALL_MINUTES, DatePart.SECOND).ge(NOTIFICATIONS.TIME))
      .fetchInto(NotificationDto.class);
  }

  public void delete(Collection<NotificationDto> notifications) {
    db.delete(NOTIFICATIONS)
      .where(NOTIFICATIONS.ID.in(map(notifications, n -> n.notificationId)))
      .execute();
  }

  @Autowired
  public NotificationRepository(DSLContext db) {
    this.db = db;
  }
}
