package org.exoplatform.officeonline;

/**
 * The Class OfficeOnlineException.
 */
public class OfficeOnlineException extends Exception {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -8981933520830552416L;


  /**
   * Instantiates a new office online exception.
   *
   * @param message the message
   */
  public OfficeOnlineException(String message) {
    super(message);
  }

  /**
   * Instantiates a new office online exception.
   *
   * @param cause the cause
   */
  public OfficeOnlineException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new office online exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public OfficeOnlineException(String message, Throwable cause) {
    super(message, cause);
  }

}