package com.solozobov.andrei.logic;

import com.solozobov.andrei.utils.Naming;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.solozobov.andrei.logic.FirstBrain.*;
import static com.solozobov.andrei.utils.Factory.list;
import static java.lang.Long.parseLong;
import static java.util.concurrent.TimeUnit.*;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * solozobov on 02.07.2019
 */
@SuppressWarnings("WeakerAccess")
public class Keyboards {
  public static InlineKeyboardButton empty() {
    return button(" ", IGNORE);
  }

  public static InlineKeyboardButton button(String text) {
    return button(text, IGNORE);
  }

  public static InlineKeyboardButton button(String text, String command) {
    return new InlineKeyboardButton(text).setCallbackData(command);
  }

  @SafeVarargs
  public static InlineKeyboardMarkup keyboard(List<InlineKeyboardButton>... buttons) {
    return new InlineKeyboardMarkup().setKeyboard(list(buttons));
  }

  public static InlineKeyboardMarkup keyboard(InlineKeyboardButton ... buttons) {
    return new InlineKeyboardMarkup().setKeyboard(list(list(buttons)));
  }

  public static InlineKeyboardMarkup dayHourMinuteSelector(@NotNull String selectorActionPrefix, @NotNull String selectorActionPostfix, @NotNull String nextActionPrefix) {
    long daysBigStep = 10;
    long hoursBigStep = 4;
    long minutesBigStep = 10;
    long daysSmallStep = 1;
    long hoursSmallStep = 1;
    long minutesSmallStep = 1;

    final long remindInSeconds = selectorActionPostfix.isEmpty() ? 15 : parseLong(selectorActionPostfix);
    long minutes = remindInSeconds % 60;
    long hours = (remindInSeconds / 60) % 24;
    long days = remindInSeconds / 60 / 24;

    final List<List<InlineKeyboardButton>> result = new ArrayList<>();
    result.add(list(button(
        "через"
        + (days == 0 ? "" : " " + Naming.daysAccusative(days))
        + (hours == 0 ? "" : " " + Naming.hoursAccusative(hours))
        + (minutes == 0 ? "" : " " + Naming.minutesAccusative(minutes)), nextActionPrefix + remindInSeconds
    )));
    result.add(list(
        button("+" + Naming.daysAccusative(daysBigStep), selectorActionPrefix + (remindInSeconds + DAYS.toMinutes(daysBigStep))),
        button("+" + Naming.hoursAccusative(hoursBigStep), selectorActionPrefix + (remindInSeconds + HOURS.toMinutes(hoursBigStep))),
        button("+" + Naming.minutesAccusative(minutesBigStep), selectorActionPrefix + (remindInSeconds + MINUTES.toMinutes(minutesBigStep)))
    ));
    result.add(list(
        button("+" + Naming.daysAccusative(daysSmallStep), selectorActionPrefix + (remindInSeconds + DAYS.toMinutes(daysSmallStep))),
        button("+" + Naming.hoursAccusative(hoursSmallStep), selectorActionPrefix + (remindInSeconds + HOURS.toMinutes(hoursSmallStep))),
        button("+" + Naming.minutesAccusative(minutesSmallStep), selectorActionPrefix + (remindInSeconds + MINUTES.toMinutes(minutesSmallStep)))
    ));
    result.add(list(
        remindInSeconds < DAYS.toMinutes(daysSmallStep) ? empty() : button("-" + Naming.daysAccusative(daysSmallStep), selectorActionPrefix + (remindInSeconds - DAYS.toMinutes(daysSmallStep))),
        remindInSeconds < HOURS.toMinutes(hoursSmallStep) ? empty() : button("-" + Naming.hoursAccusative(hoursSmallStep), selectorActionPrefix + (remindInSeconds - HOURS.toMinutes(hoursSmallStep))),
        remindInSeconds < MINUTES.toMinutes(minutesSmallStep) ? empty() : button("-" + Naming.minutesAccusative(minutesSmallStep), selectorActionPrefix + (remindInSeconds - MINUTES.toMinutes(minutesSmallStep)))
    ));
    result.add(list(
        remindInSeconds < DAYS.toMinutes(daysBigStep) ? empty() : button("-" + Naming.daysAccusative(daysBigStep), selectorActionPrefix + (remindInSeconds - DAYS.toMinutes(daysBigStep))),
        remindInSeconds < HOURS.toMinutes(hoursBigStep) ? empty() : button("-" + Naming.hoursAccusative(hoursBigStep), selectorActionPrefix + (remindInSeconds - HOURS.toMinutes(hoursBigStep))),
        remindInSeconds < MINUTES.toMinutes(minutesBigStep) ? empty() : button("-" + Naming.minutesAccusative(minutesBigStep), selectorActionPrefix + (remindInSeconds - MINUTES.toMinutes(minutesBigStep)))
    ));

    return new InlineKeyboardMarkup().setKeyboard(result);
  }

  public static InlineKeyboardMarkup dateSelector(@NotNull String selectorActionPrefix, @NotNull String selectorActionPostfix, @NotNull String nextActionPrefix,  ZoneId userTimeZone) {
    final LocalDate now = LocalDate.now(userTimeZone);
    final LocalDate fromDate = selectorActionPostfix.isEmpty() ? now : LocalDate.parse(selectorActionPostfix, DATE_FORMATTER);

    final List<List<InlineKeyboardButton>> result = new ArrayList<>();
    if (fromDate.compareTo(now) > 0) {
      LocalDate backToDate = fromDate.minusMonths(1).withDayOfMonth(1);
      if (backToDate.compareTo(now) < 0) {
        backToDate = now;
      }
      result.add(list(button("<<< " + Naming.month(backToDate) + " " + backToDate.getYear(), selectorActionPrefix + backToDate.format(DATE_FORMATTER))));
    }
    result.add(list(button(Naming.month(fromDate) + " " + fromDate.getYear())));
    result.add(list(button("пн"), button("вт"), button("ср"), button("чт"), button("пт"), button("сб"), button("вс")));

    LocalDate date = fromDate;
    Month currentMonth = date.getMonth();
    boolean monthChanged = false;
    for (int i = 0; i < 5 || !monthChanged; i++) {
      final List<InlineKeyboardButton> week = new ArrayList<>();
      for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
        if (date.equals(fromDate)  && dayOfWeek < date.getDayOfWeek().getValue()
         || date.getDayOfMonth() == 1 && dayOfWeek < date.getDayOfWeek().getValue()
         || !currentMonth.equals(date.getMonth())
        ) {
          week.add(empty());
        }
        else {
          week.add(button("" + date.getDayOfMonth(), nextActionPrefix + DATE_FORMATTER.format(date)));
          date = date.plusDays(1);
        }
      }
      result.add(week);
      if (!currentMonth.equals(date.getMonth())) {
        monthChanged = true;
        result.add(list(button(Naming.month(date) + " " + date.getYear() + " >>>", selectorActionPrefix + date.format(DATE_FORMATTER))));
        currentMonth = date.getMonth();
      }
    }
    return new InlineKeyboardMarkup().setKeyboard(result);
  }

  public static InlineKeyboardMarkup timeSelector(@NotNull LocalTime defaultNotificationTime, @NotNull String nextActionPrefix) {
    final List<List<InlineKeyboardButton>> result = new ArrayList<>();
    final String defaultTime = defaultNotificationTime.format(TIME_FORMATTER);
    List<InlineKeyboardButton> line = list(button(defaultTime, nextActionPrefix + defaultTime));
    final LocalTime time = LocalTime.of(0, 0);
    for (int i = 0; i < 24; i++) {
      if (i % 6 == 0) {
        result.add(line);
        line = new ArrayList<>();
      }
      line.add(button(i + ":00", nextActionPrefix + time.withHour(i).format(TIME_FORMATTER)));
    }
    result.add(line);
    return new InlineKeyboardMarkup().setKeyboard(result);
  }
}
