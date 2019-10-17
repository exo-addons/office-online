package org.exoplatform.officeonline.exception;

/**
 * The Class BadParameterException.
 */
public class BadParameterException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058618L;

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param message the message
   */
  public BadParameterException(String message) {
    super(message);
  }

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param cause the cause
   */
  public BadParameterException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public BadParameterException(String message, Throwable cause) {
    super(message, cause);
  }

}