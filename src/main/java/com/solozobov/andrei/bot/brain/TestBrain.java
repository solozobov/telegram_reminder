package com.solozobov.andrei.bot.brain;

import com.solozobov.andrei.bot.ButtonAction;
import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.db.NotificationRepository;
import com.solozobov.andrei.db.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.format.DateTimeFormatter;

import static com.solozobov.andrei.bot.Keyboards.timeInput;
import static com.solozobov.andrei.bot.brain.Dtos.STRING;

/**
 * solozobov on 14.11.2019
 */
//@Component
@SuppressWarnings("Convert2MethodRef")
public class TestBrain extends BaseBrain {
  private static final Logger LOG = LoggerFactory.getLogger(FirstBrain.class);

  {
    new AuthorizedMessageAction(null) {
      protected void perform3(TelegramBot bot, Message message) {
        bot.reply(message, "Введите время ", timeInput(null, input -> INPUT_TIME.getActionKey(input), time -> WRITE_TIME.getActionKey(time.format(DateTimeFormatter.ofPattern("HH:mm")))));
      }
    };
  }

  private final ButtonAction<String> INPUT_TIME = new AuthorizedButtonAction<String>("input_time", STRING) {
    protected void perform3(TelegramBot bot, Message message, String str) {
      bot.editMessage(message, str, timeInput(str, input -> INPUT_TIME.getActionKey(input), time -> WRITE_TIME.getActionKey(time.format(DateTimeFormatter.ofPattern("HH:mm")))));
    }
  };

  private final ButtonAction<String> WRITE_TIME = new AuthorizedButtonAction<String>("write_time", STRING) {
    protected void perform3(TelegramBot bot, Message message, String str) {
      bot.editMessage(message, str);
    }
  };

  public TestBrain(
      UserRepository userRepository, NotificationRepository notificationRepository
  ) {
    super(userRepository, notificationRepository);
  }
}
