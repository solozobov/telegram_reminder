package com.solozobov.andrei;

/**
 * solozobov on 01/01/2019
 */
public class RemindException extends RuntimeException {
  public RemindException(Exception e) {
    super(e);
  }

  public RemindException(String message) {
    super(message);
  }
}
