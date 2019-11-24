package com.solozobov.andrei.bot.brain;

import com.solozobov.andrei.bot.ButtonAction;
import com.solozobov.andrei.bot.MessageAction;
import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.bot.brain.Dtos.*;
import com.solozobov.andrei.db.NotificationRepository;
import com.solozobov.andrei.db.UserRepository;
import com.solozobov.andrei.utils.Serializer;
import org.jetbrains.annotations.NotNull;
import org.jooq.db.tables.records.UsersRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.*;

import static com.solozobov.andrei.bot.Keyboards.*;
import static com.solozobov.andrei.bot.TelegramBot.ADMIN_LOGIN;
import static com.solozobov.andrei.bot.brain.Dtos.*;
import static com.solozobov.andrei.utils.TelegramFormat.bold;
import static com.solozobov.andrei.utils.TelegramFormat.userLink;


/**
 * solozobov on 02.07.2019
 */
@SuppressWarnings("WeakerAccess")
public class BaseBrain {
  protected static final Logger LOG = LoggerFactory.getLogger(BaseBrain.class);
  protected static final ZoneId UTC = ZoneId.of("UTC");

  protected final UserRepository userRepository;
  protected final NotificationRepository notificationRepository;

  public BaseBrain(UserRepository userRepository, NotificationRepository notificationRepository) {
    this.userRepository = userRepository;
    this.notificationRepository = notificationRepository;
  }

  protected abstract class AuthorizedMessageAction extends MessageAction {
    protected void perform2(TelegramBot bot, Message message) {
      final UsersRecord user = getUser(message);
      if (user.getApproved()) {
        perform3(bot, message);
      } else if (ADMIN_LOGIN.equals(message.getChat().getUserName())) {
        userRepository.approve(user.getId());
        perform3(bot, message);
      } else {
        bot.write(message, "Поговорите сначала с " + userLink("Андреем", ADMIN_LOGIN) + ".\nБез его разрешения я не могу вам помогать.");
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
        bot.write(message, bold("Здравствуйте!") + "\nЯ бот Напоминатор, умею напоминать о чём угодно в удобное вам время.\nУмею напомининать разово, например о походе в театр, или регулярно, например о днях рождения или об окончании месяца.");
        final UsersRecord user = getUser(message);
        if (!user.getApproved()) {
          bot.write(message, "Кажется, вы новый пользователь. Поговорите сначала с " + userLink("Андреем", ADMIN_LOGIN) + ".\nОн вам расскажет, насколько я стабильно работаю, чего от меня стоит ожидать, а чего не стоит.");
          final UsersRecord admin = userRepository.getByLogin(ADMIN_LOGIN);
          bot.write(admin.getChatId(), "Новый пользователь хочет добавиться " + userLink(user.getFirstName() + " " + user.getLastName(), user.getChatId()), keyboard(button("принять", APPROVE_USER.getActionKey(new UserId(user)))));
        }
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
  }

  protected abstract class AuthorizedButtonAction<DATA> extends ButtonAction<DATA> {
    protected void perform2(TelegramBot bot, Message message, DATA data) {
      final UsersRecord user = getUser(message);
      if (user.getApproved()) {
        perform3(bot, message, data);
      } else if (ADMIN_LOGIN.equals(message.getChat().getUserName())) {
        userRepository.approve(user.getId());
        perform3(bot, message, data);
      } else {
        bot.write(message, "Поговорите сначала с " + userLink("Андреем", ADMIN_LOGIN) + ".\nБез его разрешения я не могу вам помогать.");
      }
    }

    abstract void perform3(TelegramBot bot, Message message, DATA data);

    AuthorizedButtonAction(String key, Serializer<DATA> serializer) {
      super(key, serializer);
    }
  }

  private final ButtonAction<UserId> APPROVE_USER = new ButtonAction<UserId>("1", USER_ID) {
    public void perform2(TelegramBot bot, Message message, UserId userId) {
      if (ADMIN_LOGIN.equals(message.getChat().getUserName())) {
        userRepository.approve(userId.id);
        bot.editMessage(message, message.getText());
      } else {
        bot.write(message, "Вы не администратор");
      }
    }
  };

  protected @NotNull UsersRecord getUser(Message message) {
    return userRepository.createOrUpdateUser(message.getChat());
  }

  protected ZoneId getUserTimeZone() {
    return ZoneId.of("Europe/Moscow");
  }

  protected LocalTime getUserDefaultNotificationTime() {
    return LocalTime.of(9, 0);
  }

  protected int getUserDefaultNotificationIntervalMinutes() {
    return 15;
  }

  protected int getUserDefaultNotificationOffsetMinutes() {
    return 15;
  }
}
