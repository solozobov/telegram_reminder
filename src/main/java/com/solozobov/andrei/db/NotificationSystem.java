package com.solozobov.andrei.db;

import com.google.common.annotations.VisibleForTesting;
import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.db.dto.NotificationDto;
import com.solozobov.andrei.utils.Threads;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * solozobov on 14/01/2019
 */
@Component
public class NotificationSystem {

  private final TelegramBot telegramBot;
  private final NotificationRepository notificationRepository;

  @VisibleForTesting void run() {
    while(true) {
      Threads.sleep(SECONDS.toMillis(1));
      final List<NotificationDto> notifications = notificationRepository.listExpired();
      if (notifications.isEmpty()) {
        return;
      }
      for (NotificationDto n : notifications) {
        telegramBot.forward(n.chatId, n.messageId); // todo: retries
      }
      notificationRepository.updateRepeated(notifications);
      notificationRepository.deleteNotRepeated(notifications);

    }
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
      final Thread loop = new Thread(this::run);
      loop.setDaemon(true);
      loop.start();
    }
  }
}
