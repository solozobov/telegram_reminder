package com.solozobov.andrei;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * solozobov on 02.07.2019
 */
public abstract class MessageAction {
  private static final Logger LOG = LoggerFactory.getLogger(MessageAction.class);
  public static final Map<String, MessageAction> actions = new HashMap<>();

  public void perform(TelegramBot bot, Message message) {
    LOG.info(message.getFrom().getUserName() + " wrote Command '" + message.getText() + "'");
    perform2(bot, message);
  }

  protected abstract void perform2(TelegramBot bot, Message message);

  public MessageAction(String message) {
    if (actions.put(message, this) != null) {
      throw new RemindException("Duplicated MessageAction for message '" + message + "'");
    }
  }
}
