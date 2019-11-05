package com.solozobov.andrei.db;

import com.solozobov.andrei.bot.TelegramBot;
import com.solozobov.andrei.db.dto.NotificationDto;
import com.solozobov.andrei.utils.Threads;
import org.springframework.beans.factory.annotation.Autowired;
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

  private void run() {
    while(true) {
      final List<NotificationDto> notifications = notificationRepository.listExpiredNotifications();
      for (NotificationDto notification : notifications) {
        telegramBot.forward(notification.chatId, notification.messageId); // retries
      }
      notificationRepository.delete(notifications);

      Threads.sleep(SECONDS.toMillis(1));
    }
  }

  @Autowired
  public NotificationSystem(TelegramBot telegramBot, NotificationRepository notificationRepository) {
    this.telegramBot = telegramBot;
    this.notificationRepository = notificationRepository;
    final Thread loop = new Thread(this::run);
    loop.setDaemon(true);
    loop.start();
  }
}
