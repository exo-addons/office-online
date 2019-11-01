package org.exoplatform.officeonline.exception;

public class ActionNotFoundException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058617L;

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param message the message
   */
  public ActionNotFoundException(String message) {
    super(message);
  }

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param cause the cause
   */
  public ActionNotFoundException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public ActionNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
