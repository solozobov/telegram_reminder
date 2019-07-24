package com.solozobov.andrei.bot;

import com.solozobov.andrei.utils.Serializer;
import org.jooq.db.tables.records.UsersRecord;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

/**
 * solozobov on 07.07.2019
 */
@SuppressWarnings("WeakerAccess")
public class Dtos {
  public static class UserId {
    public long id;

    public UserId(UsersRecord user) {
      this(user.getId());
    }

    public UserId(long id) {
      this.id = id;
    }
  }

  public static final Serializer<UserId> USER_ID = new Serializer<UserId>() {
    @Override
    public String serialize(UserId userId) {
      return "" + userId.id;
    }

    @Override
    public UserId deserialize(String string) {
      return new UserId(parseLong(string));
    }
  };

  public static class MessageId {
    public int id;

    public MessageId(Message message) {
      this(message.getMessageId());
    }

    public MessageId(int id) {
      this.id = id;
    }
  }

  public static final Serializer<MessageId> MESSAGE_ID = new Serializer<MessageId>() {
    @Override
    public String serialize(MessageId messageId) {
      return "" + messageId.id;
    }

    @Override
    public MessageId deserialize(String string) {
      return new MessageId(parseInt(string));
    }
  };

  public static class ExactNotification {
    public final int messageId;
    public final LocalDate userSelectedDate;
    public final LocalTime userSelectedTime;

    public ExactNotification(int messageId, LocalDate userSelectedDate) {
      this.messageId = messageId;
      this.userSelectedDate = userSelectedDate;
      this.userSelectedTime = LocalTime.of(0, 0);
    }

    public ExactNotification(int messageId, LocalDate userSelectedDate, LocalTime userSelectedTime) {
      this.messageId = messageId;
      this.userSelectedDate = userSelectedDate;
      this.userSelectedTime = userSelectedTime;
    }
  }

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
  private static final String DELIMITER = " ";

  public static final Serializer<ExactNotification> EXACT_NOTIFICATION = new Serializer<ExactNotification>() {

    @Override
    public String serialize(ExactNotification notification) {
      return notification.messageId + DELIMITER + DATE_FORMATTER.format(notification.userSelectedDate) + DELIMITER + TIME_FORMATTER.format(notification.userSelectedTime);
    }

    @Override
    public ExactNotification deserialize(String string) {
      final String[] parts = string.split(DELIMITER);
      return new ExactNotification(parseInt(parts[0]), LocalDate.parse(parts[1], DATE_FORMATTER), LocalTime.parse(parts[2], TIME_FORMATTER));
    }
  };

  public static class InTimeNotification {
    public final int messageId;
    public final long inMinutes;

    public InTimeNotification(int messageId, long inMinutes) {
      this.messageId = messageId;
      this.inMinutes = inMinutes;
    }
  }

  public static final Serializer<InTimeNotification> IN_TIME_NOTIFICATION = new Serializer<InTimeNotification>() {

    @Override
    public String serialize(InTimeNotification inTimeNotification) {
      return inTimeNotification.messageId + DELIMITER + inTimeNotification.inMinutes;
    }

    @Override
    public InTimeNotification deserialize(String string) {
      final String[] parts = string.split(DELIMITER);
      return new InTimeNotification(parseInt(parts[0]), parseLong(parts[1]));
    }
  };
}
