package com.solozobov.andrei.bot.brain;

import com.solozobov.andrei.utils.Serializer;
import org.jooq.db.tables.records.UsersRecord;
import org.json.JSONObject;
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

  public enum NotificationType {
    DATE    ("d"),
    INTERVAL("i");

    public static NotificationType get(String key) {
      for (NotificationType type : NotificationType.values()) {
        if (type.key.equals(key)) {
          return type;
        }
      }
      throw new RuntimeException("Can't find NotificationType for key '" + key + "'");
    }

    public final String key;

    NotificationType(String key) {
      this.key = key;
    }
  }

  public static class Notification {
    public final int messageId;
    public final LocalDate date;
    public final LocalTime time;
    public final boolean repeated;
    public final int repeatIntervalMinutes;
    public final String input;

    public Notification(
        int messageId,
        LocalDate date,
        LocalTime time,
        boolean repeated,
        int repeatIntervalMinutes,
        String input
    ) {
      this.messageId = messageId;
      this.date = date;
      this.time = time;
      this.repeated = repeated;
      this.repeatIntervalMinutes = repeatIntervalMinutes;
      this.input = input;
    }
  }

  public static final Serializer<Notification> NOTIFICATION = new Serializer<Notification>() {

    @Override
    public String serialize(Notification n) {
      return n.messageId + DELIMITER
           + DATE_FORMATTER.format(n.date) + DELIMITER
           + TIME_FORMATTER.format(n.time) + DELIMITER
           + (n.repeated ? "r" : "_") + DELIMITER
           + n.repeatIntervalMinutes + DELIMITER
           + n.input;
    }

    @Override
    public Notification deserialize(String string) {
      final String[] parts = string.split(DELIMITER, 6);
      return new Notification(
          parseInt(parts[0]),
          LocalDate.parse(parts[1], DATE_FORMATTER),
          LocalTime.parse(parts[2], TIME_FORMATTER),
          "r".equals(parts[3]),
          parseInt(parts[4]),
          parts[5]
      );
    }
  };

  public static final Serializer<JSONObject> JSON = new Serializer<JSONObject>() {
    @Override
    public String serialize(JSONObject json) {
      return json.toString();
    }

    @Override
    public JSONObject deserialize(String json) {
      return new JSONObject(json);
    }
  };

  public static final Serializer<String> STRING = new Serializer<String>() {
    @Override
    public String serialize(String str) {
      return str;
    }

    @Override
    public String deserialize(String str) {
      return str;
    }
  };
}
