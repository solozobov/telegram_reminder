package com.solozobov.andrei.bot.brain;

import com.solozobov.andrei.bot.ButtonAction;
import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.db.NotificationRepository;
import com.solozobov.andrei.db.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.solozobov.andrei.bot.brain.Dtos.*;
import static com.solozobov.andrei.bot.Keyboards.*;
import static com.solozobov.andrei.utils.Factory.list;
import static com.solozobov.andrei.utils.Naming.dayOfWeek;
import static com.solozobov.andrei.utils.Naming.monthGenitive;


/**
 * solozobov on 02.07.2019
 */
@Component
public class FirstBrain extends BaseBrain {

  @Autowired
  public FirstBrain(UserRepository userRepository, NotificationRepository notificationRepository) {
    super(userRepository, notificationRepository);
  }

  {
    new AuthorizedMessageAction(null) {
      protected void perform3(TelegramBot bot, Message message) {
        final Notification notification = new Notification(
            message.getMessageId(),
            LocalDate.now(getUserTimeZone()),
            LocalTime.now(getUserTimeZone()),
            false,
            getUserDefaultNotificationIntervalMinutes()
        );
        bot.reply(message, "Когда напомнить?", keyboard(
          list(button("конкретная дата и время", SELECT_DATE_AND_TIME.getActionKey(notification))),
          list(button("через промежуток времени", SELECT_TIME_INTERVAL_FROM_NOW.getActionKey(notification)))
        ));
      }
    };
  }

  private final ButtonAction<Notification> SELECT_DATE_AND_TIME = new AuthorizedButtonAction<Notification>("2", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      selectDate(bot, message, n, SELECT_DATE_AND_TIME, SELECT_TIME);
    }
  };

  private void selectDate(
      TelegramBot bot,
      Message message,
      Notification n,
      ButtonAction<Notification> updateAction,
      ButtonAction<Notification> saveAction
  ) {
    bot.editMessage(message, "Выберите дату", dateSelector(
        n.date,
        getUserTimeZone(),
        newDateToDisplay -> updateAction.getActionKey(new Notification(n.messageId, newDateToDisplay, n.time, n.repeated, n.repeatIntervalMinutes)),
        selectedDate -> saveAction.getActionKey(new Notification(n.messageId, selectedDate, n.time, n.repeated, n.repeatIntervalMinutes))
    ));
  }

  private final ButtonAction<Notification> SELECT_TIME = new AuthorizedButtonAction<Notification>("3", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      selectTime(bot, message, n, CREATE);
    }
  };

  private void selectTime(TelegramBot bot, Message message, Notification n, ButtonAction<Notification> saveAction) {
    bot.editMessage(message, "Выберите время", timeSelector(
        n.date,
        getUserDefaultNotificationTime(),
        getUserTimeZone(),
        selectedTime -> saveAction.getActionKey(new Notification(n.messageId, n.date, selectedTime, n.repeated, n.repeatIntervalMinutes))
    ));
  }

  private final ButtonAction<Notification> SELECT_TIME_INTERVAL_FROM_NOW = new AuthorizedButtonAction<Notification>("4", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      selectTimeIntervalFromNow(bot, message, n, SELECT_TIME_INTERVAL_FROM_NOW, CREATE);
    }
  };

  private void selectTimeIntervalFromNow(
      TelegramBot bot,
      Message message,
      Notification n,
      ButtonAction<Notification> updateAction,
      ButtonAction<Notification> saveAction
  ) {
    final ZoneId timeZone = getUserTimeZone();
    bot.editMessage(message, "Через сколько времени напомнить?", dayHourMinuteSelector(
        Duration.between(ZonedDateTime.now(timeZone), ZonedDateTime.of(n.date, n.time, timeZone)).toMinutes(),
        newMinutesToDisplay -> {
          final ZonedDateTime dateTime = ZonedDateTime.now(timeZone).plusMinutes(newMinutesToDisplay);
          return updateAction.getActionKey(new Notification(n.messageId, dateTime.toLocalDate(), dateTime.toLocalTime(), n.repeated, n.repeatIntervalMinutes));
        },
        selectedMinutes -> {
          final ZonedDateTime dateTime = ZonedDateTime.now(timeZone).plusMinutes(selectedMinutes);
          return saveAction.getActionKey(new Notification(n.messageId, dateTime.toLocalDate(), dateTime.toLocalTime(), n.repeated, n.repeatIntervalMinutes));
        }
    ));
  }

  private final ButtonAction<Notification> CREATE = new AuthorizedButtonAction<Notification>("5", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      final ZonedDateTime userSelectedDateTime = ZonedDateTime.of(n.date, n.time, getUserTimeZone());
      final LocalDateTime utcNotificationTime = userSelectedDateTime.withZoneSameInstant(UTC).toLocalDateTime();
      notificationRepository.create(message.getChatId(), n.messageId, utcNotificationTime);

      bot.editMessage(
          message, createDescription(n),
          keyboard(button("редактировать", UPDATE.getActionKey(n)))
      );
    }
  };

  private final ButtonAction<Notification> UPDATE = new AuthorizedButtonAction<Notification>("6", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      final ZonedDateTime userSelectedDateTime = ZonedDateTime.of(n.date, n.time, getUserTimeZone());
      final LocalDateTime utcNotificationTime = userSelectedDateTime.withZoneSameInstant(UTC).toLocalDateTime();
      notificationRepository.update(message.getChatId(), n.messageId, utcNotificationTime, n.repeatIntervalMinutes);
      final List<List<InlineKeyboardButton>> buttons = new ArrayList<>(6);
      buttons.add(list(button("дату напоминания", UPDATE_DATE.getActionKey(n))));
      buttons.add(list(button("время напоминания", UPDATE_TIME.getActionKey(n))));
      buttons.add(list(button("промежуток напоминания", UPDATE_TIME_INTERVAL_FROM_NOW.getActionKey(n))));
      if (n.repeated) {
        buttons.add(list(button("интервал повторения", SELECT_REPEAT_INTERVAL.getActionKey(n))));
        buttons.add(list(button("перестать повторять", UPDATE.getActionKey(new Notification(n.messageId, n.date, n.time, false, n.repeatIntervalMinutes)))));
      } else {
        buttons.add(list(button("сделать повторяющимся", SELECT_REPEAT_INTERVAL.getActionKey(n))));
      }
      buttons.add(list(button("закончить редактирование", CLOSE_EDIT.getActionKey(n))));

      bot.editMessage(message, createDescription(n) + "\nЧто изменить?", keyboard(buttons));
    }
  };

  private final ButtonAction<Notification> UPDATE_DATE = new AuthorizedButtonAction<Notification>("7", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      selectDate(bot, message, n, UPDATE_DATE, UPDATE);
    }
  };

  private final ButtonAction<Notification> UPDATE_TIME = new AuthorizedButtonAction<Notification>("8", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      selectTime(bot, message, n, UPDATE);
    }
  };

  private final ButtonAction<Notification> UPDATE_TIME_INTERVAL_FROM_NOW = new AuthorizedButtonAction<Notification>("9", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      selectTimeIntervalFromNow(bot, message, n, UPDATE_TIME_INTERVAL_FROM_NOW, UPDATE);
    }
  };

  private final ButtonAction<Notification> SELECT_REPEAT_INTERVAL = new AuthorizedButtonAction<Notification>("a", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      bot.editMessage(message, "Выберите интервал повторения напоминания", dayHourMinuteSelector(
          getUserDefaultNotificationIntervalMinutes(),
          newMinutesToDisplay -> SELECT_REPEAT_INTERVAL.getActionKey(new Notification(n.messageId, n.date, n.time, n.repeated, newMinutesToDisplay.intValue())),
          selectedRepeatIntervalMinutes -> UPDATE.getActionKey(new Notification(n.messageId, n.date, n.time, true, selectedRepeatIntervalMinutes.intValue()))
      ));
    }
  };

  private final ButtonAction<Notification> CLOSE_EDIT = new AuthorizedButtonAction<Notification>("b", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      bot.editMessage(message, createDescription(n), keyboard(button("редактировать", UPDATE.getActionKey(n))));
    }
  };

  private String createDescription(Notification n) {
    final ZonedDateTime userSelectedDateTime = ZonedDateTime.of(n.date, n.time, getUserTimeZone());
    return String.format(
        "Напоминание установлено на %s %s %s %s %s %s",
        userSelectedDateTime.format(DateTimeFormatter.ofPattern("H:mm O")),
        dayOfWeek(userSelectedDateTime.getDayOfWeek()),
        userSelectedDateTime.getDayOfMonth(),
        monthGenitive(userSelectedDateTime.getMonth()),
        userSelectedDateTime.getYear(),
        n.repeated ? " с повторением каждые " + n.repeatIntervalMinutes + " минут" : ""
    );
  }
}
