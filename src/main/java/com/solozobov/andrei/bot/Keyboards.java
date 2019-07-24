package com.solozobov.andrei.bot;

import com.solozobov.andrei.utils.Naming;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.solozobov.andrei.TelegramBot.NO_ACTION_BUTTON;
import static com.solozobov.andrei.utils.Factory.list;
import static java.util.concurrent.TimeUnit.*;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * solozobov on 02.07.2019
 */

// https://core.telegram.org/bots#keyboards
// https://tlgrm.ru/docs/bots/api#inlinekeyboardmarkup

@SuppressWarnings("WeakerAccess")
public class Keyboards {
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  public static InlineKeyboardButton empty() {
    return button(" ", NO_ACTION_BUTTON);
  }

  public static InlineKeyboardButton button(String text) {
    return button(text, NO_ACTION_BUTTON);
  }

  public static InlineKeyboardButton button(String text, @NotNull String command) {
    return new InlineKeyboardButton(text).setCallbackData(command);
  }

  @SafeVarargs
  public static InlineKeyboardMarkup keyboard(List<InlineKeyboardButton> ... buttons) {
    return new InlineKeyboardMarkup().setKeyboard(list(buttons));
  }

  public static InlineKeyboardMarkup keyboard(InlineKeyboardButton ... buttons) {
    return new InlineKeyboardMarkup().setKeyboard(list(list(buttons)));
  }

  public static InlineKeyboardMarkup dateSelector(
      LocalDate selectedDate,
      ZoneId userTimeZone,
      Function<LocalDate, String> dateSwitchAction,
      Function<LocalDate, String> dateSelectAction
  ) {
    final LocalDate userCurrentDate = LocalDate.now(userTimeZone);

    final List<List<InlineKeyboardButton>> result = new ArrayList<>();
    if (selectedDate.compareTo(userCurrentDate) > 0) {
      LocalDate backToDate = selectedDate.minusMonths(1).withDayOfMonth(1);
      if (backToDate.compareTo(userCurrentDate) < 0) {
        backToDate = userCurrentDate;
      }
      result.add(list(button("<<< " + Naming.month(backToDate) + " " + backToDate.getYear(), dateSwitchAction.apply(backToDate))));
    }
    result.add(list(button(Naming.month(selectedDate) + " " + selectedDate.getYear())));
    result.add(list(button("пн"), button("вт"), button("ср"), button("чт"), button("пт"), button("сб"), button("вс")));

    LocalDate date = selectedDate;
    Month currentMonth = date.getMonth();
    boolean monthChanged = false;
    for (int i = 0; i < 5 || !monthChanged; i++) {
      final List<InlineKeyboardButton> week = new ArrayList<>();
      for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
        if (date.equals(selectedDate)  && dayOfWeek < date.getDayOfWeek().getValue()
         || date.getDayOfMonth() == 1 && dayOfWeek < date.getDayOfWeek().getValue()
         || !currentMonth.equals(date.getMonth())
        ) {
          week.add(empty());
        }
        else {
          week.add(button("" + date.getDayOfMonth(), dateSelectAction.apply(date)));
          date = date.plusDays(1);
        }
      }
      result.add(week);
      if (!currentMonth.equals(date.getMonth())) {
        monthChanged = true;
        result.add(list(button(Naming.month(date) + " " + date.getYear() + " >>>", dateSwitchAction.apply(date))));
        currentMonth = date.getMonth();
      }
    }
    return new InlineKeyboardMarkup().setKeyboard(result);
  }

  public static InlineKeyboardMarkup timeSelector(
      LocalDate date,
      LocalTime defaultNotificationTime,
      ZoneId userTimeZone,
      Function<LocalTime, String> timeSelectAction
  ) {
    ZonedDateTime time;
    if (LocalDate.now(userTimeZone).equals(date)) {
      time = ZonedDateTime.now(userTimeZone).plusHours(1).withMinute(0).withSecond(0).withNano(0);
    } else {
      time = ZonedDateTime.of(date, LocalTime.of(0, 0), userTimeZone);
    }

    final List<List<InlineKeyboardButton>> result = new ArrayList<>();
    if (ZonedDateTime.of(date, defaultNotificationTime, userTimeZone).compareTo(time) > 0) {
      result.add(list(button(
          defaultNotificationTime.format(TIME_FORMATTER),
          timeSelectAction.apply(defaultNotificationTime)
      )));
    }

    final ZonedDateTime nextDate = time.plusDays(1).withHour(0);
    List<InlineKeyboardButton> line = new ArrayList<>();
    int i = 1;
    while(time.compareTo(nextDate) < 0) {
      line.add(button(time.getHour() + ":00", timeSelectAction.apply(time.toLocalTime())));
      if (i % 6 == 0) {
        result.add(line);
        line = new ArrayList<>();
      }
      time = time.plusHours(1);
      i++;
    }
    if (!line.isEmpty()) {
      result.add(line);
    } else if (result.isEmpty()) {
      line.add(button("23:59", timeSelectAction.apply(LocalTime.of(23, 59))));
      result.add(line);
    }

    return new InlineKeyboardMarkup().setKeyboard(result);
  }

  public static InlineKeyboardMarkup dayHourMinuteSelector(
      long currentMinutes,
      Function<Long, String> minutesSwitchAction,
      Function<Long, String> minutesSelectAction
  ) {
    long daysBigStep = 10;
    long hoursBigStep = 4;
    long minutesBigStep = 10;
    long daysSmallStep = 1;
    long hoursSmallStep = 1;
    long minutesSmallStep = 1;

    long minutes = currentMinutes % 60;
    long hours = (currentMinutes / 60) % 24;
    long days = currentMinutes / 60 / 24;

    final List<List<InlineKeyboardButton>> result = new ArrayList<>();
    result.add(list(button(
        "через"
        + (days == 0 ? "" : " " + Naming.daysAccusative(days))
        + (hours == 0 ? "" : " " + Naming.hoursAccusative(hours))
        + (minutes == 0 ? "" : " " + Naming.minutesAccusative(minutes)),
        minutesSelectAction.apply(currentMinutes)
    )));
    result.add(list(
        button("+" + Naming.daysAccusative(daysBigStep), minutesSwitchAction.apply(currentMinutes + DAYS.toMinutes(daysBigStep))),
        button("+" + Naming.hoursAccusative(hoursBigStep), minutesSwitchAction.apply(currentMinutes + HOURS.toMinutes(hoursBigStep))),
        button("+" + Naming.minutesAccusative(minutesBigStep), minutesSwitchAction.apply(currentMinutes + MINUTES.toMinutes(minutesBigStep)))
    ));
    result.add(list(
        button("+" + Naming.daysAccusative(daysSmallStep), minutesSwitchAction.apply(currentMinutes + DAYS.toMinutes(daysSmallStep))),
        button("+" + Naming.hoursAccusative(hoursSmallStep), minutesSwitchAction.apply(currentMinutes + HOURS.toMinutes(hoursSmallStep))),
        button("+" + Naming.minutesAccusative(minutesSmallStep), minutesSwitchAction.apply(currentMinutes + MINUTES.toMinutes(minutesSmallStep)))
    ));
    result.add(list(
        currentMinutes < DAYS.toMinutes(daysSmallStep) ? empty() : button("-" + Naming.daysAccusative(daysSmallStep), minutesSwitchAction.apply(currentMinutes + DAYS.toMinutes(daysSmallStep))),
        currentMinutes < HOURS.toMinutes(hoursSmallStep) ? empty() : button("-" + Naming.hoursAccusative(hoursSmallStep), minutesSwitchAction.apply(currentMinutes + HOURS.toMinutes(hoursSmallStep))),
        currentMinutes < MINUTES.toMinutes(minutesSmallStep) ? empty() : button("-" + Naming.minutesAccusative(minutesSmallStep), minutesSwitchAction.apply(currentMinutes + MINUTES.toMinutes(minutesSmallStep)))
    ));
    result.add(list(
        currentMinutes < DAYS.toMinutes(daysBigStep) ? empty() : button("-" + Naming.daysAccusative(daysBigStep), minutesSwitchAction.apply(currentMinutes + DAYS.toMinutes(daysBigStep))),
        currentMinutes < HOURS.toMinutes(hoursBigStep) ? empty() : button("-" + Naming.hoursAccusative(hoursBigStep), minutesSwitchAction.apply(currentMinutes + HOURS.toMinutes(hoursBigStep))),
        currentMinutes < MINUTES.toMinutes(minutesBigStep) ? empty() : button("-" + Naming.minutesAccusative(minutesBigStep), minutesSwitchAction.apply(currentMinutes + MINUTES.toMinutes(minutesBigStep)))
    ));

    return new InlineKeyboardMarkup().setKeyboard(result);
  }
}
