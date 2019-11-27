package com.solozobov.andrei;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * solozobov on 26.11.2019
 */
public class TestTelegramBotsApi extends TelegramBotsApi {
  public TestTelegramBotsApi() {}

  @Override
  public BotSession registerBot(LongPollingBot bot) {
    return new DefaultBotSession();
  }
}
