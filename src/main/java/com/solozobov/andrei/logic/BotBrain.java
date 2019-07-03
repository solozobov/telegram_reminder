package com.solozobov.andrei.logic;

import com.solozobov.andrei.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

/**
 * solozobov on 02.07.2019
 */
public interface BotBrain {

  void defaultMessageResponse(TelegramBot bot, Message message);

  Map<String, MessageAction> getMessageActions();

  void defaultButtonResponse(TelegramBot bot, Message message, String buttonAction);

  Map<String, ButtonAction> getButtonActions();
}
