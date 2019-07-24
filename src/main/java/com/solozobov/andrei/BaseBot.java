package com.solozobov.andrei;

import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

/**
 * solozobov on 09.07.2019
 */
public abstract class BaseBot extends TelegramLongPollingBot {
  private final String botName;
  private final String botToken;

  @Override
  public String getBotUsername() {
    return botName;
  }

  @Override
  public String getBotToken() {
    return botToken;
  }

  protected BaseBot(String botName, String botToken, DefaultBotOptions options) {
    super(options);
    this.botName = botName;
    this.botToken = botToken;
  }
}
