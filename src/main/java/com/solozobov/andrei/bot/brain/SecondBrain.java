package com.solozobov.andrei.bot.brain;

import com.solozobov.andrei.bot.ButtonAction;
import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.bot.brain.Dtos.*;
import com.solozobov.andrei.db.NotificationRepository;
import com.solozobov.andrei.db.UserRepository;
import com.solozobov.andrei.utils.Naming;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.solozobov.andrei.bot.Keyboards.*;
import static com.solozobov.andrei.bot.brain.Dtos.*;
import static com.solozobov.andrei.utils.Factory.list;
import static com.solozobov.andrei.utils.Naming.dayOfWeek;
import static com.solozobov.andrei.utils.Naming.monthGenitive;


/**
 * solozobov on 02.07.2019
 */
//@Component
public class SecondBrain extends BaseBrain {

  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("H:mm");

  @Autowired
  public SecondBrain(UserRepository userRepository, NotificationRepository notificationRepository) {
    super(userRepository, notificationRepository);
  }

  {
    new AuthorizedMessageAction(null) {
      protected void perform3(TelegramBot bot, Message message) {
        final ZonedDateTime dateTime = ZonedDateTime.now(getUserTimeZone()).plusMinutes(
            getUserDefaultNotificationOffsetMinutes());
        bot.reply(message, "Когда напомнить?", getKeyboard(new Notification(
            message.getMessageId(),
            dateTime.toLocalDate(),
            dateTime.toLocalTime(),
            false,
            getUserDefaultNotificationIntervalMinutes()
        )));
      }
    };
  }

  private final ButtonAction<Notification> SETUP = new AuthorizedButtonAction<Notification>("2", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification notification) {
      bot.editMessage(message, "Когда напомнить?", getKeyboard(notification));
    }
  };

  private InlineKeyboardMarkup getKeyboard(Notification n) {
    final List<List<InlineKeyboardButton>> buttons = new ArrayList<>(6);
//    if (n.type == DATE) {
      buttons.add(list(button("дата", SETUP.getActionKey(new Notification(n.messageId, n.date, n.time, n.repeated, n.repeatIntervalMinutes)))));
      buttons.add(list(button(n.date.getDayOfMonth() + " " + monthGenitive(n.date) + " " + n.date.getYear(), SET_DATE.getActionKey(n))));
      buttons.add(list(button(TIME.format(n.time), SET_TIME.getActionKey(n))));
//    } else {
      final ZoneId timeZone = getUserTimeZone();
      final long minutes = Duration.between(ZonedDateTime.now(timeZone), ZonedDateTime.of(n.date, n.time, timeZone)).toMinutes();

      buttons.add(list(button("через", SETUP.getActionKey(new Notification(n.messageId, n.date, n.time, n.repeated, n.repeatIntervalMinutes)))));
      buttons.add(list(button(Naming.timeAccusative(minutes), SET_INTERVAL.getActionKey(n))));
//    }
    if (n.repeated) {
      buttons.add(list(button("повторять", SETUP.getActionKey(new Notification(n.messageId, n.date, n.time, false, n.repeatIntervalMinutes)))));
      buttons.add(list(button("каждые " + n.repeatIntervalMinutes + " минут", SET_REPEAT_INTERVAL.getActionKey(n))));
    } else {
      buttons.add(list(button("не повторять", SETUP.getActionKey(new Notification(n.messageId, n.date, n.time, true, n.repeatIntervalMinutes)))));
    }
    buttons.add(list(button("установить напоминание", SAVE.getActionKey(n))));
    return keyboard(buttons);
  }

  private final ButtonAction<Notification> SET_DATE = new AuthorizedButtonAction<Notification>("3", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      bot.editMessage(message, "Выберите дату", dateSelector(
          n.date,
          getUserTimeZone(),
          newDateToDisplay -> getActionKey(new Notification(n.messageId, newDateToDisplay, n.time, n.repeated, n.repeatIntervalMinutes)),
          selectedDate -> SETUP.getActionKey(new Notification(n.messageId, selectedDate, n.time, n.repeated, n.repeatIntervalMinutes))
      ));
    }
  };

  private final ButtonAction<Notification> SET_TIME = new AuthorizedButtonAction<Notification>("4", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      bot.editMessage(message, "Выберите время", timeSelector(
          n.date,
          getUserDefaultNotificationTime(),
          getUserTimeZone(),
          selectedTime -> SETUP.getActionKey(new Notification(n.messageId, n.date, selectedTime, n.repeated, n.repeatIntervalMinutes))
      ));
    }
  };

  private final ButtonAction<Notification> SET_INTERVAL = new AuthorizedButtonAction<Notification>("5", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      final ZoneId timeZone = getUserTimeZone();
      bot.editMessage(message, "Выберите время", dayHourMinuteSelector(
          Duration.between(ZonedDateTime.now(timeZone), ZonedDateTime.of(n.date, n.time, timeZone)).toMinutes(),
          newMinutesToDisplay -> {
            final ZonedDateTime dateTime = ZonedDateTime.now(timeZone).plusMinutes(newMinutesToDisplay);
            return SET_REPEAT_INTERVAL.getActionKey(new Notification(n.messageId, dateTime.toLocalDate(), dateTime.toLocalTime(), n.repeated, n.repeatIntervalMinutes));
          },
          selectedMinutes -> {
            final ZonedDateTime dateTime = ZonedDateTime.now(timeZone).plusMinutes(selectedMinutes);
            return SETUP.getActionKey(new Notification(n.messageId, dateTime.toLocalDate(), dateTime.toLocalTime(), n.repeated, n.repeatIntervalMinutes));
          }
      ));
    }
  };

  private final ButtonAction<Notification> SET_REPEAT_INTERVAL = new AuthorizedButtonAction<Notification>("6", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      bot.editMessage(message, "Выберите время", dayHourMinuteSelector(
          n.repeatIntervalMinutes,
          newMinutesToDisplay -> SET_REPEAT_INTERVAL.getActionKey(new Notification(n.messageId, n.date, n.time, n.repeated, newMinutesToDisplay.intValue())),
          selectedMinutes -> SETUP.getActionKey(new Notification(n.messageId, n.date, n.time, n.repeated, selectedMinutes.intValue()))
      ));
    }
  };

  private final ButtonAction<Notification> SAVE = new AuthorizedButtonAction<Notification>("7", NOTIFICATION) {
    protected void perform3(TelegramBot bot, Message message, Notification n) {
      final ZonedDateTime userSelectedDateTime = ZonedDateTime.of(n.date, n.time, getUserTimeZone());
      final LocalDateTime utcNotificationTime = userSelectedDateTime.withZoneSameInstant(UTC).toLocalDateTime();
      notificationRepository.create(message.getChatId(), n.messageId, utcNotificationTime);

      bot.editMessage(
          message,
          String.format(
              "Напоминание установлено на %s %s %s %s %s %s",
              userSelectedDateTime.format(DateTimeFormatter.ofPattern("H:mm O")),
              dayOfWeek(userSelectedDateTime.getDayOfWeek()),
              userSelectedDateTime.getDayOfMonth(),
              monthGenitive(userSelectedDateTime.getMonth()),
              userSelectedDateTime.getYear(),
              n.repeated ? " с повторением каждые " + n.repeatIntervalMinutes + " минут" : ""
      ));
    }
  };
}
