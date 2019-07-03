package com.solozobov.andrei;

import com.solozobov.andrei.db.UserRepository;
import com.solozobov.andrei.logic.BotBrain;
import com.solozobov.andrei.logic.ButtonAction;
import com.solozobov.andrei.logic.MessageAction;
import org.apache.http.client.config.RequestConfig;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * solozobov on 09/12/2018
 * https://core.telegram.org/bots/api#inlinekeyboardbutton
 */
@Component
public class TelegramBot extends TelegramLongPollingBot {

  private Logger LOG = LoggerFactory.getLogger(TelegramBot.class);

  private final UserRepository userRepository;
  private final String botName;
  private final String botToken;
  private final BotBrain botBrain;

  @Override
  public String getBotUsername() {
    return botName;
  }

  @Override
  public String getBotToken() {
    return botToken;
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      final Message message = update.getMessage();
      final Chat chat = message.getChat();
      if (chat.isUserChat()) {
        userRepository.createOrUpdateUser(chat);
        for (Map.Entry<String, MessageAction> entry : botBrain.getMessageActions().entrySet()) {
          if (entry.getKey().equals(message.getText())) {
            entry.getValue().perform(this, message);
            return;
          }
        }
        botBrain.defaultMessageResponse(this, message);
      }
    }
    else if (update.hasCallbackQuery()) {
      final CallbackQuery callback = update.getCallbackQuery();
      final Message message = callback.getMessage();
      final String buttonAction = callback.getData();
      for (Map.Entry<String, ButtonAction> entry : botBrain.getButtonActions().entrySet()) {
        final String actionPrefix = entry.getKey();
        if (buttonAction.startsWith(actionPrefix)) {
          entry.getValue().perform(this, message, buttonAction.substring(actionPrefix.length()));
          return;
        }
      }
      botBrain.defaultButtonResponse(this, message, buttonAction);
    }
  }

  public void write(Message message, String text) {
    write(message.getChatId(), text);
  }

  public void write(long chatId, String text) {
    try {
      this.execute(new SendMessage(chatId, text));
    } catch (TelegramApiException e) {
      LOG.error("Failed sending message '" + text + "' to chat #" + chatId, e);
    }
  }

  public void forward(long chatId, int messageId) {
    try {
      this.execute(new ForwardMessage(chatId, chatId, messageId));
    } catch (TelegramApiException e) {
      LOG.error("Failed forwarding message #" + messageId + " to chat #" + chatId, e);
    }
  }

  public void reply(Message message, String text, @Nullable InlineKeyboardMarkup keyboard) {
    try {
      this.execute(
          new SendMessage(message.getChatId(), text)
              .setReplyToMessageId(message.getMessageId())
              .setReplyMarkup(keyboard)
      );
    } catch (TelegramApiException e) {
      LOG.error("Failed replying to message in chat #" + message.getChatId() + " with '" + text + "'" + (keyboard == null ? "" : " and keyboard"), e);
    }
  }

  public void editMessage(
      Message message,
      String newMessageText,
      @Nullable Consumer<State> stateUpdater,
      @Nullable InlineKeyboardMarkup keyboard
  ) {
    editMessage(
        message,
        state -> {
          if (stateUpdater != null) {
            stateUpdater.accept(state);
          }
          return newMessageText;
        },
        keyboard
    );
  }

  public void editMessage(
      Message message,
      Function<State, String> messageGenerator,
      @Nullable InlineKeyboardMarkup keyboard
  ) {
    try {
      final State state = State.deserialize(message.getText());
      final String newMessageText = messageGenerator.apply(state);
      this.execute(
          new EditMessageText()
              .setChatId(message.getChatId())
              .setMessageId(message.getMessageId())
              .setText(state.serialize() + newMessageText)
              .setReplyMarkup(keyboard)
      );
    } catch (TelegramApiException e) {
      LOG.error("Failed editing message in chat #" + message.getChatId() + (keyboard == null ? "" : " and keyboard"), e);
    }
  }

  @Autowired
  public TelegramBot(
      BotBrain botBrain,
      UserRepository userRepository,
      TelegramBotsApi telegramBotsApi,
      @Value("remember.bot.name") String botName,
      @Value("remember.bot.token") String botToken
  ) {
    super(createOptions());
    this.userRepository = userRepository;
    this.botBrain = botBrain;
    this.botName = botName;
    this.botToken = botToken;
    try {
      telegramBotsApi.registerBot(this);
    } catch (Exception e) {
      throw new RemindException(e);
    }
  }

  private static DefaultBotOptions createOptions() {
    try {
//      final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//      while(interfaces.hasMoreElements()) {
//        final NetworkInterface networkInterface = interfaces.nextElement();
//        System.out.println(networkInterface);
//        final Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
//        while(addresses.hasMoreElements()) {
//          System.out.println("  " + addresses.nextElement());
//        }
//      }
      //final NetworkInterface networkInterface = NetworkInterface.getByName("en0");
      final DefaultBotOptions options = new DefaultBotOptions();
      final NetworkInterface networkInterface = NetworkInterface.getByName("utun1");
      if (networkInterface == null) {
        System.out.println("No VPN tunnel");
      } else {
        final InetAddress localAddress = networkInterface.getInetAddresses().nextElement();
        options.setRequestConfig(RequestConfig.custom().setLocalAddress(localAddress).build());
      }
      return options;
    } catch (SocketException e) {
      throw new RemindException(e);
    }
  }
}
