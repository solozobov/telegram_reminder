package com.solozobov.andrei.logic;

import com.solozobov.andrei.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * solozobov on 02.07.2019
 */
public interface MessageAction {
  void perform(TelegramBot bot, Message message);
}
