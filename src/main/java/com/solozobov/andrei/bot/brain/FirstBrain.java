package com.solozobov.andrei.bot.brain;

import com.solozobov.andrei.bot.ButtonAction;
import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.db.NotificationRepository;
import com.solozobov.andrei.db.UserRepository;
import com.solozobov.andrei.bot.brain.Dtos.ExactNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static com.solozobov.andrei.bot.brain.Dtos.*;
import static com.solozobov.andrei.bot.Keyboards.*;
import static com.solozobov.andrei.utils.Factory.list;
import static com.solozobov.andrei.utils.Naming.dayOfWeek;
import static com.solozobov.andrei.utils.Naming.monthGenitive;


/**
 * solozobov on 02.07.2019
 */
//@Component
public class FirstBrain extends BaseBrain {

  @Autowired
  public FirstBrain(UserRepository userRepository, NotificationRepository notificationRepository) {
    super(userRepository, notificationRepository);
  }

  {
    new AuthorizedMessageAction(null) {
      protected void perform3(TelegramBot bot, Message message) {
        bot.reply(message, "Каким будет это напоминание?", keyboard(
            button("разовым", REMIND_ONCE.getActionKey(new MessageId(message))),
            button("повторяющимся", REMIND_REPEATED.getActionKey(new MessageId(message)))
        ));
      }
    };
  }

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

      final ZonedDateTime userSelectedDateTime = dateTimeUtc.atZone(UTC).withZoneSameInstant(getUserTimeZone());
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
}
