package com.solozobov.andrei.bot;

import com.solozobov.andrei.utils.Naming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.solozobov.andrei.bot.TelegramBot.NO_ACTION_BUTTON;
import static com.solozobov.andrei.utils.Factory.list;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
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
  public static InlineKeyboardMarkup keyboard(@NotNull List<InlineKeyboardButton> ... buttons) {
    return keyboard(list(buttons));
  }

  public static InlineKeyboardMarkup keyboard(@NotNull InlineKeyboardButton ... buttons) {
    return keyboard(list(list(buttons)));
  }

  public static InlineKeyboardMarkup keyboard(@NotNull List<List<InlineKeyboardButton>> buttons) {
    return new InlineKeyboardMarkup().setKeyboard(buttons);
  }

  public static InlineKeyboardMarkup dateSelector(
      @NotNull LocalDate selectedDate,
      @NotNull ZoneId userTimeZone,
      @NotNull Function<LocalDate, String> dateSwitchAction,
      @NotNull Function<LocalDate, String> dateSelectAction
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

  public static InlineKeyboardMarkup timeSelectorOld(
      @NotNull LocalDate date,
      @NotNull LocalTime defaultNotificationTime,
      @NotNull ZoneId userTimeZone,
      @NotNull Function<LocalTime, String> timeSelectAction
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

  public static InlineKeyboardMarkup timeInput(
      @Nullable String currentInput,
      @NotNull Function<String, String> nextInput,
      @NotNull Function<LocalTime, String> selectTime
  ) {
    final List<List<InlineKeyboardButton>> result = new ArrayList<>();
    if (currentInput == null || currentInput.isEmpty()) {
      result.add(list(button("00:00", selectTime.apply(LocalTime.of(0, 0)))));
      result.add(list(button("1", nextInput.apply("1")),  button("2", nextInput.apply("2")),  button("3", nextInput.apply("03"))));
      result.add(list(button("4", nextInput.apply("04")), button("5", nextInput.apply("05")), button("6", nextInput.apply("06"))));
      result.add(list(button("7", nextInput.apply("07")), button("8", nextInput.apply("08")), button("9", nextInput.apply("09"))));
      result.add(list(button(":00", selectTime.apply(LocalTime.of(0, 0))), button("0", nextInput.apply("0")), button(":30", selectTime.apply(LocalTime.of(0, 30)))));
    } else if (currentInput.length() == 1) {
      final int hour = parseInt(currentInput) * 10;
      result.add(list(button(currentInput + "0:00", selectTime.apply(LocalTime.of(hour, 0)))));
      if ("2".equals(currentInput)) {
        result.add(list(button("1", nextInput.apply("21")), button("2", nextInput.apply("22")), button("3", nextInput.apply("23"))));
        result.add(list(empty(), empty(), empty()));
        result.add(list(empty(), empty(), empty()));
        result.add(list(button(":00", selectTime.apply(LocalTime.of(2, 0))), button("0", nextInput.apply("20")), button(":30", selectTime.apply(LocalTime.of(2, 30)))));
      } else {
        result.add(list(button("1", nextInput.apply(currentInput + "1")), button("2", nextInput.apply(currentInput + "2")), button("3", nextInput.apply(currentInput + "3"))));
        result.add(list(button("4", nextInput.apply(currentInput + "4")), button("5", nextInput.apply(currentInput + "5")), button("6", nextInput.apply(currentInput + "6"))));
        result.add(list(button("7", nextInput.apply(currentInput + "7")), button("8", nextInput.apply(currentInput + "8")), button("9", nextInput.apply(currentInput + "9"))));
        result.add(list(button(":00", selectTime.apply(LocalTime.of(hour, 0))), button("0", nextInput.apply(currentInput + "0")), button(":30", selectTime.apply(LocalTime.of(hour, 30)))));
      }
    } else if (currentInput.length() == 2) {
      final int hour = parseInt(currentInput);
      result.add(list(button(currentInput + ":00", selectTime.apply(LocalTime.of(hour, 0)))));
      result.add(list(button("1", nextInput.apply(currentInput + "1")), button("2", nextInput.apply(currentInput + "2")), button("3", nextInput.apply(currentInput + "3"))));
      result.add(list(button("4", nextInput.apply(currentInput + "4")), button("5", nextInput.apply(currentInput + "5")), button("6", selectTime.apply(LocalTime.of(hour, 6)))));
      result.add(list(button("7", selectTime.apply(LocalTime.of(hour, 7))), button("8", selectTime.apply(LocalTime.of(hour, 8))), button("9", selectTime.apply(LocalTime.of(hour, 9)))));
      result.add(list(button(":00", selectTime.apply(LocalTime.of(hour, 0))), button("0", nextInput.apply(currentInput + "0")), button(":30", selectTime.apply(LocalTime.of(hour, 30)))));
    } else {
      final int hour = parseInt(currentInput.substring(0, 2));
      final int minute = parseInt(currentInput.substring(2)) * 10;
      result.add(list(button(hour + ":" + format("%02d", minute), selectTime.apply(LocalTime.of(hour, minute)))));
      result.add(list(button("1", selectTime.apply(LocalTime.of(hour, minute + 1))), button("2", selectTime.apply(LocalTime.of(hour, minute + 2))), button("3", selectTime.apply(LocalTime.of(hour, minute + 3)))));
      result.add(list(button("4", selectTime.apply(LocalTime.of(hour, minute + 4))), button("5", selectTime.apply(LocalTime.of(hour, minute + 5))), button("6", selectTime.apply(LocalTime.of(hour, minute + 6)))));
      result.add(list(button("7", selectTime.apply(LocalTime.of(hour, minute + 7))), button("8", selectTime.apply(LocalTime.of(hour, minute + 8))), button("9", selectTime.apply(LocalTime.of(hour, minute + 9)))));
      result.add(list(button(":00", selectTime.apply(LocalTime.of(hour, 0))), button("0", selectTime.apply(LocalTime.of(hour, minute))), button(":30", selectTime.apply(LocalTime.of(hour, 30)))));
    }
    return new InlineKeyboardMarkup().setKeyboard(result);
  }

  public static InlineKeyboardMarkup dayHourMinuteSelector(
      @NotNull String descriptionPrefix,
      long currentMinutes,
      @NotNull Function<Long, String> minutesSwitchAction,
      @NotNull Function<Long, String> minutesSelectAction
  ) {
    final long daysBigStep = 10;
    final long hoursBigStep = 4;
    final long minutesBigStep = 10;
    final long daysSmallStep = 1;
    final long hoursSmallStep = 1;
    final long minutesSmallStep = 1;

    final String text = Naming.timeAccusative(currentMinutes);

    final List<List<InlineKeyboardButton>> result = new ArrayList<>();
    result.add(list(button(descriptionPrefix + " " + text, minutesSelectAction.apply(currentMinutes))));
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
        currentMinutes < DAYS.toMinutes(daysSmallStep) ? empty() : button("-" + Naming.daysAccusative(daysSmallStep), minutesSwitchAction.apply(currentMinutes - DAYS.toMinutes(daysSmallStep))),
        currentMinutes < HOURS.toMinutes(hoursSmallStep) ? empty() : button("-" + Naming.hoursAccusative(hoursSmallStep), minutesSwitchAction.apply(currentMinutes - HOURS.toMinutes(hoursSmallStep))),
        currentMinutes < MINUTES.toMinutes(minutesSmallStep) ? empty() : button("-" + Naming.minutesAccusative(minutesSmallStep), minutesSwitchAction.apply(currentMinutes - MINUTES.toMinutes(minutesSmallStep)))
    ));
    result.add(list(
        currentMinutes < DAYS.toMinutes(daysBigStep) ? empty() : button("-" + Naming.daysAccusative(daysBigStep), minutesSwitchAction.apply(currentMinutes - DAYS.toMinutes(daysBigStep))),
        currentMinutes < HOURS.toMinutes(hoursBigStep) ? empty() : button("-" + Naming.hoursAccusative(hoursBigStep), minutesSwitchAction.apply(currentMinutes - HOURS.toMinutes(hoursBigStep))),
        currentMinutes < MINUTES.toMinutes(minutesBigStep) ? empty() : button("-" + Naming.minutesAccusative(minutesBigStep), minutesSwitchAction.apply(currentMinutes - MINUTES.toMinutes(minutesBigStep)))
    ));

    return new InlineKeyboardMarkup().setKeyboard(result);
  }
}
