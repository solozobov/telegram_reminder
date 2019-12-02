package com.solozobov.andrei.db;

import com.google.common.annotations.VisibleForTesting;
import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.db.dto.NotificationDto;
import com.solozobov.andrei.utils.Exceptions;
import com.solozobov.andrei.utils.LoopThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * solozobov on 14/01/2019
 */
@Component
public class NotificationSystem {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationSystem.class);
  private static final Integer NOT_FOUND = 400;

  private final TelegramBot telegramBot;
  private final NotificationRepository notificationRepository;

  @VisibleForTesting void loop() {
    final List<NotificationDto> notifications = notificationRepository.listExpired();
    if (notifications.isEmpty()) {
      return;
    }
    for (NotificationDto n : notifications) {
      try {
        telegramBot.forward(n.chatId, n.messageId); // todo: retries
      } catch (Exception e) {
        final TelegramApiRequestException telegramException = Exceptions.findCause(e, TelegramApiRequestException.class);
        if (telegramException != null) {
          if (NOT_FOUND.equals(telegramException.getErrorCode())) {
            final String errorMessage = telegramException.getApiResponse();
            if (errorMessage != null && errorMessage.contains("message to forward not found")) {
              LOG.info("Deleted notification for chat#" + n.chatId + " deleted message#" + n.messageId);
              notificationRepository.delete(n.chatId, n.messageId);
            }
          }
        }
      }
    }
    notificationRepository.updateRepeated(notifications);
    notificationRepository.deleteNotRepeated(notifications);
  }

  @Autowired
  public NotificationSystem(
      TelegramBot telegramBot,
      NotificationRepository notificationRepository,
      @Value("${remember.notification.enabled}") boolean enabled
  ) {
    this.telegramBot = telegramBot;
    this.notificationRepository = notificationRepository;
    if (enabled) {
      new LoopThread("notifications_sender", this::loop, SECONDS.toMillis(1)).start();
    }
  }
}
