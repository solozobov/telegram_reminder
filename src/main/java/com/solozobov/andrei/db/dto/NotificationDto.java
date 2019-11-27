package com.solozobov.andrei.db.dto;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

/**
 * solozobov on 14/01/2019
 */
public class NotificationDto {
  public final long notificationId;
  public final long chatId;
  public final int messageId;
  public final @NotNull LocalDateTime time;
  public final @Nullable Integer repeatIntervalMinutes;

  public NotificationDto(
      long notificationId,
      long chatId,
      int messageId,
      @NotNull LocalDateTime time,
      @Nullable Integer forestallMinutes,
      @Nullable Integer repeatIntervalMinutes
  ) {
    this.notificationId = notificationId;
    this.chatId = chatId;
    this.messageId = messageId;
    this.time = time;
    this.repeatIntervalMinutes = repeatIntervalMinutes;
  }
}
