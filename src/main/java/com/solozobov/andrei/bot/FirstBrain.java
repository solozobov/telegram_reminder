package com.solozobov.andrei.bot;

import com.solozobov.andrei.ButtonAction;
import com.solozobov.andrei.MessageAction;
import com.solozobov.andrei.TelegramBot;
import com.solozobov.andrei.db.NotificationRepository;
import com.solozobov.andrei.db.UserRepository;
import com.solozobov.andrei.bot.Dtos.ExactNotification;
import com.solozobov.andrei.bot.Dtos.UserId;
import com.solozobov.andrei.utils.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jooq.db.tables.records.UsersRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static com.solozobov.andrei.bot.Dtos.*;
import static com.solozobov.andrei.bot.Keyboards.*;
import static com.solozobov.andrei.utils.Factory.list;
import static com.solozobov.andrei.utils.Naming.dayOfWeek;
import static com.solozobov.andrei.utils.Naming.monthGenitive;
import static com.solozobov.andrei.utils.TelegramFormat.*;


/**
 * solozobov on 02.07.2019
 */
@Component
public class FirstBrain {
  private static final String ANDREI_LOGIN = "mimimotik";
  private static final int ANDREI_CHAT_ID = 109580470;
  private static final int YANA_CHAT_ID = 498779902;
  private static final Logger LOG = LoggerFactory.getLogger(FirstBrain.class);
  private static final ZoneId UTC = ZoneId.of("UTC");

  private final UserRepository userRepository;
  private final NotificationRepository notificationRepository;

  @Autowired
  public FirstBrain(UserRepository userRepository, NotificationRepository notificationRepository) {
    this.userRepository = userRepository;
    this.notificationRepository = notificationRepository;
  }

  private abstract class AuthorizedMessageAction extends MessageAction {
    protected void perform2(TelegramBot bot, Message message) {
      final UsersRecord user = getUser(message);
      if (user.getApproved()) {
        perform3(bot, message);
      } else if (ANDREI_LOGIN.equals(message.getChat().getUserName()) || message.getChatId() == YANA_CHAT_ID) {
        userRepository.approve(user.getId());
        perform3(bot, message);
      } else {
        bot.write(message, "Поговорите сначала с " + userLink("Андреем", ANDREI_LOGIN) + ".\nБез его разрешения я не могу вам помогать.");
      }
    }

    abstract void perform3(TelegramBot bot, Message message);

    AuthorizedMessageAction(String key) {
      super(key);
    }
  }

  {
    new MessageAction("/start") {
      protected void perform2(TelegramBot bot, Message message) {
        LOG.info("/start " + message);
        bot.write(message, bold("Здравствуйте!") + "\nЯ бот Напоминатор, умею напоминать о чём угодно в удобное вам время.\nУмею напомининать разово, напрмиер о походе в театр, или регулярно, например о днях рождения или об окончании месяца.");
        final UsersRecord user = getUser(message);
        if (!user.getApproved()) {
          bot.write(message, "Кажется, вы новый пользователь. Поговорите сначала с " + userLink("Андреем", ANDREI_LOGIN) + ".\nОн вам расскажет, насколько я стабильно работаю, чего от меня стоит ожидать, а чего не стоит.");
          bot.write(ANDREI_CHAT_ID, "Новый пользователь хочет добавиться " + userLink(user.getFirstName() + " " + user.getLastName(), user.getChatId()), keyboard(button("принять", APPROVE_USER.getActionKey(new UserId(user)))));
        }
      }
    };

    new AuthorizedMessageAction("/create") {
      protected void perform3(TelegramBot bot, Message message) {
        bot.write(message, "О чём вам напомнить?");
      }
    };

    new AuthorizedMessageAction("/list") {
      protected void perform3(TelegramBot bot, Message message) {
      }
    };

    new AuthorizedMessageAction("/settings") {
      protected void perform3(TelegramBot bot, Message message) {
        bot.write(message, "Время отправки регулярных оповещений: 9:00 MSK");
      }
    };

    new AuthorizedMessageAction(null) {
      protected void perform3(TelegramBot bot, Message message) {
        bot.reply(message, "Каким будет это напоминание?", keyboard(
            button("разовым", REMIND_ONCE.getActionKey(new MessageId(message))),
            button("повторяющимся", REMIND_REPEATED.getActionKey(new MessageId(message)))
        ));
      }
    };
  }

  private abstract class AuthorizedButtonAction<DATA> extends ButtonAction<DATA> {
    protected void perform2(TelegramBot bot, Message message, DATA data) {
      final UsersRecord user = getUser(message);
      if (user.getApproved()) {
        perform3(bot, message, data);
      } else if (ANDREI_LOGIN.equals(message.getChat().getUserName()) || message.getChatId() == YANA_CHAT_ID) {
        userRepository.approve(user.getId());
        perform3(bot, message, data);
      } else {
        bot.write(message, "Поговорите сначала с " + userLink("Андреем", ANDREI_LOGIN) + ".\nБез его разрешения я не могу вам помогать.");
      }
    }

    abstract void perform3(TelegramBot bot, Message message, DATA data);

    AuthorizedButtonAction(String key, Serializer<DATA> serializer) {
      super(key, serializer);
    }
  }

  private final ButtonAction<UserId> APPROVE_USER = new ButtonAction<UserId>("1", USER_ID) {
    public void perform2(TelegramBot bot, Message message, UserId userId) {
      if (ANDREI_LOGIN.equals(message.getChat().getUserName())) {
        userRepository.approve(userId.id);
        bot.editMessage(message, message.getText());
      } else {
        bot.write(message, "Вы не Андрей");
      }
    }
  };

  private final ButtonAction<MessageId> REMIND_ONCE = new AuthorizedButtonAction<MessageId>("2", MESSAGE_ID) {
    protected void perform3(TelegramBot bot, Message message, MessageId messageId) {
      bot.editMessage(message, "Когда напомнить?", keyboard(
          list(button("конкретная дата", REMIND_EXACT_SELECT_DATE.getActionKey(new ExactNotification(messageId.id, LocalDate.now(getUserTimeZone()))))),
          list(button("через промежуток времени", REMIND_IN_TIME.getActionKey(new InTimeNotification(messageId.id, 15L))))
      ));
    }
  };

  private final ButtonAction<ExactNotification> REMIND_EXACT_SELECT_DATE = new AuthorizedButtonAction<ExactNotification>("3", EXACT_NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, ExactNotification notification) {
      bot.editMessage(message, "Выберите дату", dateSelector(
          notification.userSelectedDate,
          getUserTimeZone(),
          newDateToDisplay -> getActionKey(new ExactNotification(notification.messageId, newDateToDisplay)),
          selectedDate -> REMIND_EXACT_SELECT_TIME.getActionKey(new ExactNotification(notification.messageId, selectedDate))
      ));
    }
  };

  private final ButtonAction<ExactNotification> REMIND_EXACT_SELECT_TIME = new AuthorizedButtonAction<ExactNotification>("4", EXACT_NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, ExactNotification notification) {
      bot.editMessage(message, "Выберите время", timeSelector(
          notification.userSelectedDate,
          getUserDefaultNotificationTime(),
          getUserTimeZone(),
          selectedTime -> REMIND_EXACT_CREATE.getActionKey(new ExactNotification(notification.messageId, notification.userSelectedDate, selectedTime))
      ));
    }
  };

  private final ButtonAction<ExactNotification> REMIND_EXACT_CREATE = new AuthorizedButtonAction<ExactNotification>("5", EXACT_NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, ExactNotification notification) {
      final ZonedDateTime userSelectedDateTime = ZonedDateTime.of(
          notification.userSelectedDate, notification.userSelectedTime, getUserTimeZone());
      final LocalDateTime utcNotificationTime = userSelectedDateTime.withZoneSameInstant(UTC).toLocalDateTime();
      notificationRepository.create(message.getChatId(), notification.messageId, utcNotificationTime);

      bot.editMessage(
          message,
          String.format(
              "Напоминание установлено на %s %s %s %s %s",
              userSelectedDateTime.format(DateTimeFormatter.ofPattern("HH:mm O")),
              dayOfWeek(userSelectedDateTime.getDayOfWeek()),
              userSelectedDateTime.getDayOfMonth(),
              monthGenitive(userSelectedDateTime.getMonth()),
              userSelectedDateTime.getYear()
      ));
    }
  };

  private final ButtonAction<InTimeNotification> REMIND_IN_TIME = new AuthorizedButtonAction<InTimeNotification>("6", IN_TIME_NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, InTimeNotification notification) {
      bot.editMessage(message, "Выберите время", dayHourMinuteSelector(
          notification.inMinutes,
          newInMinutesToDisplay -> REMIND_IN_TIME.getActionKey(new InTimeNotification(notification.messageId, newInMinutesToDisplay)),
          selectedInMinutes -> REMIND_IN_TIME_CREATE.getActionKey(new InTimeNotification(notification.messageId, selectedInMinutes))
      ));
    }
  };

  private final ButtonAction<InTimeNotification> REMIND_IN_TIME_CREATE = new AuthorizedButtonAction<InTimeNotification>("7", IN_TIME_NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, InTimeNotification notification) {
      final LocalDateTime dateTimeUtc = LocalDateTime.now(UTC).plusMinutes(notification.inMinutes);
      notificationRepository.create(message.getChatId(), notification.messageId, dateTimeUtc);

      final ZonedDateTime userSelectedDateTime = dateTimeUtc.atZone(getUserTimeZone());
      bot.editMessage(
          message,
          String.format(
              "Напоминание установлено на %s %s %s %s %s",
              userSelectedDateTime.format(DateTimeFormatter.ofPattern("HH:mm O")),
              dayOfWeek(userSelectedDateTime.getDayOfWeek()),
              userSelectedDateTime.getDayOfMonth(),
              monthGenitive(userSelectedDateTime.getMonth()),
              userSelectedDateTime.getYear()
      ));
    }
  };

  private final ButtonAction<MessageId> REMIND_REPEATED = new AuthorizedButtonAction<MessageId>("8", MESSAGE_ID) {
    protected void perform3(TelegramBot bot, Message message, MessageId messageId) {
      bot.write(message, "Извините, я пока этого не умею :(");
    }
  };


  private @NotNull UsersRecord getUser(Message message) {
    return userRepository.createOrUpdateUser(message.getChat());
  }

  private ZoneId getUserTimeZone() {
    return ZoneId.of("Europe/Moscow");
  }

  private LocalTime getUserDefaultNotificationTime() {
    return LocalTime.of(9, 0);
  }

}
