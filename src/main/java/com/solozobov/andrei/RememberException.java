package com.solozobov.andrei;

/**
 * solozobov on 01/01/2019
 */
public class RememberException extends RuntimeException {
  public RememberException(Exception e, String messageTemplate, Object ... arguments) {
    super(String.format(messageTemplate, arguments), e);
  }

  public RememberException(Exception e, String message) {
    super(message, e);
  }

  public RememberException(Exception e) {
    super(e);
  }

  public RememberException(String message) {
    super(message);
  }

  public RememberException(String messageTemplate, Object ... arguments) {
    super(String.format(messageTemplate, arguments));
  }
}
