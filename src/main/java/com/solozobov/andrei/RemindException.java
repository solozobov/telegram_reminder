package com.solozobov.andrei;

/**
 * solozobov on 01/01/2019
 */
public class RemindException extends RuntimeException {
  public RemindException(Exception e, String message) {
    super(message, e);
  }

  public RemindException(Exception e) {
    super(e);
  }

  public RemindException(String message) {
    super(message);
  }

  public RemindException(String messageTemplate, Object ... arguments) {
    super(String.format(messageTemplate, arguments));
  }
}
