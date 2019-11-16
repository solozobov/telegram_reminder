package com.solozobov.andrei.bot.brain;

import com.solozobov.andrei.bot.MessageAction;
import com.solozobov.andrei.bot.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import static com.solozobov.andrei.utils.TelegramFormat.bold;

/**
 * solozobov on 14.11.2019
 */
//@Component
public class TestBrain {
  private static final Logger LOG = LoggerFactory.getLogger(FirstBrain.class);

  {
    new MessageAction("/start") {
      protected void perform2(TelegramBot bot, Message message) {
        LOG.info("/start " + message);
        bot.write2(message, bold("Привет!"));
      }
    };

  }
}
