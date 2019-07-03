package com.solozobov.andrei.db.dto;

/**
 * solozobov on 14/01/2019
 */
public class NotificationDto {
  public final long notificationId;
  public final long chatId;
  public final int messageId;

  public NotificationDto(long notificationId, long chatId, int messageId) {
    this.notificationId = notificationId;
    this.chatId = chatId;
    this.messageId = messageId;
  }
}
