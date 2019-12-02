package com.solozobov.andrei.bot;

import com.google.common.annotations.VisibleForTesting;
import com.solozobov.andrei.RememberException;
import com.solozobov.andrei.db.SettingSystem;
import com.solozobov.andrei.utils.Exceptions;
import com.solozobov.andrei.utils.Factory;
import com.solozobov.andrei.utils.State;
import org.apache.http.client.config.RequestConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.solozobov.andrei.SettingKeys.BACKUP_CHAT_ID;
import static java.util.Collections.list;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

/**
 * solozobov on 09/12/2018
 * https://core.telegram.org/bots/api#inlinekeyboardbutton
 *
 * checking ip-address https://blocklist.rkn.gov.ru
 */
@Component("telegramBot")
@SuppressWarnings("WeakerAccess")
public class TelegramBot extends BaseBot {
  public static final String ADMIN_LOGIN = "mimimotik";

  public static final String NO_ACTION_BUTTON = "_";

  private static final Logger LOG = LoggerFactory.getLogger(TelegramBot.class);

  private final SettingSystem settingSystem;

  @Override
  public void onUpdateReceived(Update update) {
    try {
      onInnerUpdateReceived(update);
    } catch (Exception e) {
      final TelegramApiRequestException tae = Exceptions.findCause(e, TelegramApiRequestException.class);
      if (tae != null) {
        LOG.error(tae.getApiResponse() + " " + tae.getErrorCode() + " " + tae.getParameters(), e);
      } else {
        LOG.error("", e);
      }
    }
  }

  private void onInnerUpdateReceived(Update update) {
    if (update.hasMessage()) {
      final Message message = update.getMessage();
      for (Map.Entry<String, MessageAction> entry : MessageAction.actions.entrySet()) {
        if (entry.getKey() != null && entry.getKey().equals(message.getText())) {
          entry.getValue().perform(this, message);
          return;
        }
      }
      final MessageAction defaultAction = MessageAction.actions.get(null);
      if (defaultAction == null) {
        LOG.error("Default action not defined " + message);
      } else {
        defaultAction.perform(this, message);
      }
    }
    else if (update.hasCallbackQuery()) {
      final CallbackQuery callback = update.getCallbackQuery();
      final Message message = callback.getMessage();
      final String buttonAction = callback.getData();
      if (NO_ACTION_BUTTON.equals(buttonAction)) {
        return;
      }

      for (ButtonAction action : ButtonAction.actions) {
        if (action.accepts(buttonAction)) {
          action.perform(this, message, buttonAction);
          return;
        }
      }

      LOG.error("Unknown button action '" + buttonAction + "'");
    }
    else {
      LOG.info("Unknown update " + update);
    }
  }

  public void write(Message message, String text) {
    write(message.getChatId(), text);
  }

  public void write(long chatId, String text) {
    write(chatId, text, null);
  }

  public void write(long chatId, String text, @Nullable InlineKeyboardMarkup keyboard) {
    try {
      this.execute(new SendMessage(chatId, text).setReplyMarkup(keyboard).enableMarkdown(true));
    } catch (TelegramApiException e) {
      throw new RememberException(e, "Failed sending message '" + text + "' to chat #" + chatId);
    }
  }

  public void write2(Message message, String text) {
    try {
      final KeyboardRow row1 = new KeyboardRow();
      row1.add("A");
      row1.add("B");
      final KeyboardRow row2 = new KeyboardRow();
      row2.add("C");
      this.execute(new SendMessage(message.getChatId(), text).setReplyMarkup(new ReplyKeyboardMarkup().setKeyboard(Factory.list(row1, row2))).enableMarkdown(true));
    } catch (TelegramApiException e) {
      throw new RememberException(e, "Failed sending message '" + text + "' to chat #" + message.getChatId());
    }
  }

  public void forward(long chatId, int messageId) {
    try {
      this.execute(new ForwardMessage(chatId, chatId, messageId));
    } catch (TelegramApiException e) {
      throw new RememberException(e, "Failed forwarding message #" + messageId + " to chat #" + chatId);
    }
  }

  public void reply(Message message, String text, @Nullable InlineKeyboardMarkup keyboard) {
    try {
      this.execute(
          new SendMessage(message.getChatId(), text)
              .setReplyToMessageId(message.getMessageId())
              .setReplyMarkup(keyboard)
              .enableMarkdown(true)
      );
    } catch (TelegramApiException e) {
      throw new RememberException(e, "Failed replying to message in chat #" + message.getChatId() + " with '" + text + "'" + (keyboard == null ? "" : " and keyboard"));
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
              .enableMarkdown(true)
      );
    } catch (TelegramApiException e) {
      throw new RememberException(e,
                                  "Failed editing message in chat #" + message.getChatId() + (keyboard == null ? "" : " and keyboard"));
    }
  }

  public void editMessage(Message message, String newMessageText) {
    editMessage(message, newMessageText, null);
  }

  public void editMessage(Message message, String newMessageText, @Nullable InlineKeyboardMarkup keyboard) {
    try {
      this.execute(
          new EditMessageText()
              .setChatId(message.getChatId())
              .setMessageId(message.getMessageId())
              .setText(newMessageText)
              .setReplyMarkup(keyboard)
              .enableMarkdown(true)
      );
    } catch (TelegramApiException e) {
      throw new RememberException(
          e, "Failed editing message in chat #" + message.getChatId() + (keyboard == null ? "" : " and keyboard"));
    }
  }

  public void sendFile(long chatId, File file) {
    try {
      this.execute(new SendDocument().setChatId(chatId).setDocument(file));
    } catch (TelegramApiException e) {
      throw new RememberException(e, "Failed sending file " + file.getAbsolutePath());
    }
  }

  {
    new MessageAction("/backup_here") {
      protected void perform2(TelegramBot bot, Message message) {
        if (ADMIN_LOGIN.equals(message.getFrom().getUserName())) {
          settingSystem.set(BACKUP_CHAT_ID, message.getChat().getId());
          bot.write(message, "Дампы базы буду сохранять здесь");
        } else {
          bot.write(message, "Вы не Андрей");
        }
      }
    };
  }

  @Autowired
  public TelegramBot(
      TelegramBotsApi telegramBotsApi,
      SettingSystem settingSystem,
      @Value("${remember.bot.name}") String botName,
      @Value("${remember.bot.token}") String botToken,
      @Value("${remember.telegram.discovery.enabled}") boolean discoveryEnabled
  ) {
    super(botName, botToken, discoveryEnabled ? createOptions(botName, botToken) : new DefaultBotOptions());
    this.settingSystem = settingSystem;
    try {
      telegramBotsApi.registerBot(this);
    } catch (Exception e) {
      throw new RememberException(e);
    }
  }

  @VisibleForTesting protected static DefaultBotOptions createOptions(String botName, String botToken) {
    try {
      final InetAddress inetAddress = findLocalAddress(botName, botToken);
      LOG.info("Using local address " + inetAddress + " to connect to Telegram");
      return createOptions(RequestConfig.custom().setLocalAddress(inetAddress));
    } catch (SocketException e) {
      throw new RememberException(e);
    }
  }

  private static @NotNull InetAddress findLocalAddress(String botName, String botToken) throws SocketException {
    final List<InetAddress> addresses = list(NetworkInterface.getNetworkInterfaces())
        .stream()
        .filter(networkInterface -> {
          try {
            return !networkInterface.isLoopback();
          } catch (SocketException e) {
            LOG.info("Failed to analyze " + networkInterface);
            return false;
          }
        })
        .sorted(comparingInt(i -> i.getName().contains("utun1") ? 0 : 1))
        .flatMap(i -> list(i.getInetAddresses()).stream().sorted(comparingInt(a -> a instanceof Inet6Address ? 0 : 1)))
        .collect(toList());

    int timeoutMillis = 100;
    while (true) {
      for (InetAddress address : addresses) {
        if (check(botName, botToken, address, timeoutMillis)) {
          return address;
        }
      }
      timeoutMillis *= 2;
      System.out.println("Raising socket timeout to " + timeoutMillis + " millis");
    }
  }

  private static boolean check(String botName, String botToken, InetAddress address, int timeoutMillis) {
    try (ConnectionTestBot bot = new ConnectionTestBot(botName, botToken, address, timeoutMillis)) {
      bot.clearWebhook();
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private static class ConnectionTestBot extends BaseBot implements AutoCloseable {
    ConnectionTestBot(String botName, String botToken, InetAddress address, int timeoutMillis) {
      super(botName, botToken, createOptions(RequestConfig.custom().setLocalAddress(address).setConnectTimeout(timeoutMillis)));
    }

    @Override
    public void onUpdateReceived(Update update) {}

    @Override
    public void close() {
      this.onClosing();
    }
  }

  private static DefaultBotOptions createOptions(RequestConfig.Builder config) {
    final DefaultBotOptions options = new DefaultBotOptions();
    options.setRequestConfig(config.build());
    return options;
  }
}
