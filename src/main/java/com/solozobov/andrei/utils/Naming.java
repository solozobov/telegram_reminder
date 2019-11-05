package com.solozobov.andrei.utils;

import com.solozobov.andrei.RememberException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

/**
 * solozobov on 13/01/2019
 */
public class Naming {

  public static String month(LocalDate date) {
    switch (date.getMonthValue()) {
      case 1: return "Январь";
      case 2: return "Февраль";
      case 3: return "Март";
      case 4: return "Апрель";
      case 5: return "Май";
      case 6: return "Июнь";
      case 7: return "Июль";
      case 8: return "Август";
      case 9: return "Сентябрь";
      case 10: return "Октябрь";
      case 11: return "Ноябрь";
      case 12: return "Декабрь";
      default: throw new RememberException("Unsupported month value " + date.getMonthValue() + " " + date);
    }
  }

  public static String monthGenitive(LocalDate date) {
    return monthGenitive(date.getMonth());
  }

  public static String monthGenitive(Month month) {
    switch (month.getValue()) {
      case 1: return "Января";
      case 2: return "Февраля";
      case 3: return "Марта";
      case 4: return "Апреля";
      case 5: return "Мая";
      case 6: return "Июня";
      case 7: return "Июля";
      case 8: return "Августа";
      case 9: return "Сентября";
      case 10: return "Октября";
      case 11: return "Ноября";
      case 12: return "Декабря";
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
}
