package com.solozobov.andrei.utils;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * solozobov on 04.07.2019
 */
public class Exceptions {

  @Nullable
  public static <T extends Throwable> T findCause(@Nullable Throwable throwable, Class<T> causeToFind) {
    final Throwable result = findCause(throwable, Collections.singleton(causeToFind));
    if(result == null) {
      return null;
    }
    else {
      //noinspection unchecked
      return (T)result;
    }
  }

  @Nullable
  public static Throwable findCause(
      @Nullable Throwable throwable, Collection<Class<? extends Throwable>> exceptionsToFind
  ) {
    if(throwable == null) {
      return null;
    }

    for(final Class<? extends Throwable> exceptionToFind : exceptionsToFind) {
      if(exceptionToFind.isAssignableFrom(throwable.getClass())) {
        return throwable;
      }
    }

    Throwable result = findCause(throwable.getCause(), exceptionsToFind);
    if(result != null) {
      return result;
    }

    for(Throwable suppressed : throwable.getSuppressed()) {
      result = findCause(suppressed, exceptionsToFind);
      if(result != null) {
        return result;
      }
    }
    return null;
  }
}
