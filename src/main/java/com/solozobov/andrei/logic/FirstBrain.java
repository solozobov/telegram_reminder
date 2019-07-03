package com.solozobov.andrei.logic;

import com.solozobov.andrei.TelegramBot;
import com.solozobov.andrei.db.NotificationRepository;
import com.solozobov.andrei.utils.Factory;
import com.solozobov.andrei.utils.Naming;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.solozobov.andrei.State.NotificationType.ONCE;
import static com.solozobov.andrei.State.NotificationType.REPEATED;
import static com.solozobov.andrei.logic.Keyboards.*;
import static com.solozobov.andrei.utils.Factory.list;
import static java.lang.Long.parseLong;

/**
 * solozobov on 02.07.2019
 */
@Component
public class FirstBrain implements BotBrain {
  static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
  static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  public  static final String IGNORE              = "ignore";
  private static final String CREATE_YEARLY       = "yearly";
  private static final String CREATE_MONTHLY      = "monthly";
  private static final String CREATE_ONCE         = "single";
  private static final String REMIND_IN_TIME_     = "in_time_";
  private static final String SET_REMIND_IN_TIME_ = "set_in_time_";
  private static final String REMIND_EXACT        = "remind_exact";
  private static final String REMIND_EXACT_DATE_  = "exact_date_";
  private static final String REMIND_EXACT_TIME_  = "exact_time_";

  private final Map<String, MessageAction> messageActions;

  private final Map<String, ButtonAction> buttonActions;

  @Override
  public void defaultMessageResponse(TelegramBot bot, Message message) {
    bot.reply(message, "Каким будет это напоминание?", keyboard(
        button("ежегодным", CREATE_YEARLY),
        button("ежемесячным", CREATE_MONTHLY),
        button("разовым", CREATE_ONCE)
    ));
  }

  @Override
  public Map<String, MessageAction> getMessageActions() {
    return messageActions;
  }

  @Override
  public void defaultButtonResponse(TelegramBot bot, Message message, String buttonAction) {
    bot.write(message, "Я не знаю кнопки " + buttonAction);
  }

  @Override
  public Map<String, ButtonAction> getButtonActions() {
    return buttonActions;
  }

  private ZoneId getUserTimeZone(Message message) {
    return ZoneId.of("Europe/Moscow");
  }

  private LocalTime getUserDefaultNotificationTime(Message message) {
    return LocalTime.of(9, 0);
  }

  @Autowired
  public FirstBrain(NotificationRepository notificationRepository) {
    this.messageActions = Factory.mapOf(
        "/start", (b, m) -> b.write(m, "Здравствуйте! Я могу напомнить вам о чём попросите. Могу напоминанть о разовых событиях, могу делать регулярные напоминания."),
        "/add", (b, m) -> b.write(m, "Напиши мне текст напоминания"),
        "/list", (b, m) -> {},
        "/settings", (b, m) -> b.write(m, "Время отправки ежедневных оповещений: 9:00 MSK")
    );

    this.buttonActions = Factory.mapOf(
        REMIND_EXACT_DATE_, (b, m, postfix) -> {
          final LocalDate date = LocalDate.parse(postfix, DATE_FORMATTER);
          b.editMessage(
              m,
              String.format(
                  "В какое время %s %s %s %s вам напомнить? Выберите вариант",
                  Naming.dayOfWeekGenitive(date), date.getDayOfMonth(), Naming.monthGenitive(date), date.getYear()
              ),
              state -> state.date = date,
              timeSelector(getUserDefaultNotificationTime(m), REMIND_EXACT_TIME_)
          );
        },
        REMIND_EXACT_TIME_, (b, m, postfix) -> {
          final LocalTime time = LocalTime.parse(postfix, TIME_FORMATTER);
          b.editMessage(
              m,
              state -> {
                final LocalDate date = state.date;
                notificationRepository.create(
                    m.getChatId(),
                    m.getReplyToMessage().getMessageId(),
                    LocalDateTime.of(state.date, time)
                );
                return String.format(
                    "Напоминание установлено на %s %s %s %s %s",
                    time.format(TIME_FORMATTER),
                    Naming.dayOfWeek(date), date.getDayOfMonth(), Naming.monthGenitive(date), date.getYear()
                );
              },
              null
          );
        },
        REMIND_EXACT, (b, m, postfix) -> b.editMessage(m, state -> "Выберите дату", dateSelector(REMIND_EXACT, postfix, REMIND_EXACT_DATE_, getUserTimeZone(m))),
        REMIND_IN_TIME_, (b, m, postfix) -> b.editMessage(m, state -> "Выберите время", dayHourMinuteSelector(REMIND_IN_TIME_, postfix, SET_REMIND_IN_TIME_)),
        SET_REMIND_IN_TIME_, (b, m, postfix) -> b.editMessage(
            m,
            state -> {
              final long minutes = parseLong(postfix);
              final LocalDateTime dateTime = LocalDateTime.now(getUserTimeZone(m)).plusMinutes(minutes);
              notificationRepository.create(
                  m.getChatId(),
                  m.getReplyToMessage().getMessageId(),
                  dateTime
              );
              return String.format(
                  "Напоминание установлено на %s %s %s %s %s",
                  dateTime.format(TIME_FORMATTER),
                  Naming.dayOfWeek(dateTime.getDayOfWeek()), dateTime.getDayOfMonth(), Naming.monthGenitive(dateTime.getMonth()), dateTime.getYear()
              );
            },
            null
        ),
        CREATE_ONCE, (b, m, postfix) -> b.editMessage(m, "Когда напомнить?", state -> state.notificationType = ONCE, keyboard(
            list(button("конкретное время", REMIND_EXACT)),
            list(button("через время", REMIND_IN_TIME_))
        )),
        REMIND_IN_TIME_, (b, m, postfix) -> {},
        CREATE_MONTHLY, (b, m, postfix) -> b.editMessage(m, "Создаём повторяющееся напоминание", state -> state.notificationType = REPEATED, null),
        IGNORE, (b, m, postfix) -> {}
    );
  }
}
