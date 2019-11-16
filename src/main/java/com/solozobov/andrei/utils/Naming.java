package com.solozobov.andrei.utils;

import com.solozobov.andrei.RememberException;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

/**
 * solozobov on 13/01/2019
 */
public class Naming {

  public static String month(LocalDate date) {
    switch (date.getMonthValue()) {
      case 1: return "январь";
      case 2: return "февраль";
      case 3: return "март";
      case 4: return "апрель";
      case 5: return "май";
      case 6: return "июнь";
      case 7: return "июль";
      case 8: return "август";
      case 9: return "сентябрь";
      case 10: return "октябрь";
      case 11: return "ноябрь";
      case 12: return "декабрь";
      default: throw new RememberException("Unsupported month value " + date.getMonthValue() + " " + date);
    }
  }

  public static String monthGenitive(LocalDate date) {
    return monthGenitive(date.getMonth());
  }

  public static String monthGenitive(Month month) {
    switch (month.getValue()) {
      case 1: return "января";
      case 2: return "февраля";
      case 3: return "марта";
      case 4: return "апреля";
      case 5: return "мая";
      case 6: return "июня";
      case 7: return "июля";
      case 8: return "августа";
      case 9: return "сентября";
      case 10: return "октября";
      case 11: return "ноября";
      case 12: return "декабря";
      default: throw new RememberException("Unsupported " + month);
    }
  }

  public static String dayOfWeek(LocalDate date) {
    return dayOfWeek(date.getDayOfWeek());
  }

  public static String dayOfWeek(DayOfWeek dayOfWeek) {
    switch (dayOfWeek.getValue()) {
      case 1: return "понедельник";
      case 2: return "вторник";
      case 3: return "среду";
      case 4: return "четверг";
      case 5: return "пятницу";
      case 6: return "субботу";
      case 7: return "воскресенье";
      default: throw new RememberException("Unexpected " + dayOfWeek);
    }
  }

  public static String dayOfWeekGenitive(LocalDate date) {
    switch (date.getDayOfWeek().getValue()) {
      case 1: return "понедельника";
      case 2: return "вторника";
      case 3: return "среды";
      case 4: return "четверга";
      case 5: return "пятницы";
      case 6: return "субботы";
      case 7: return "воскресенья";
      default: throw new RememberException("Unexpected " + date.getDayOfWeek());
    }
  }

  public static String minutesAccusative(long minutes) {
    if (11 <= minutes && minutes <= 20) return minutes + " минут";
    long remain = minutes % 10;
    if (remain == 0 || 5 <= remain) return minutes + " минут";
    if (remain == 1) return minutes + " минуту";
    return minutes + " минуты";
  }

  public static String hoursAccusative(long hours) {
    if (11 <= hours && hours <= 20) return hours + " часов";
    long remain = hours % 10;
    if (remain == 0 || 5 <= remain) return hours + " часов";
    if (remain == 1) return hours + " час";
    return hours + " часа";
  }

  public static String daysAccusative(long days) {
    if (11 <= days && days <= 20) return days + " дней";
    long remain = days % 10;
    if (remain == 0 || 5 <= remain) return days + " дней";
    if (remain == 1) return days + " день";
    return days + " дня";
  }

  public static String timeAccusative(long timeMinutes) {
    long minutes = timeMinutes % 60;
    long hours = (timeMinutes / 60) % 24;
    long days = timeMinutes / 60 / 24;
    return (days == 0 ? "" : " " + Naming.daysAccusative(days))
         + (hours == 0 ? "" : " " + Naming.hoursAccusative(hours))
         + (minutes == 0 ? "" : " " + Naming.minutesAccusative(minutes));
  }

}
