package org.exoplatform.officeonline.exception;

/**
 * The Class IllegalFileNameException.
 */
public class IllegalFileNameException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058617L;


  /**
   * Instantiates a new illegal file name exception.
   *
   * @param message the message
   */
  public IllegalFileNameException(String message) {
    super(message);
  }


  /**
   * Instantiates a new illegal file name exception.
   *
   * @param cause the cause
   */
  public IllegalFileNameException(Throwable cause) {
    super(cause);
  }


  /**
   * Instantiates a new illegal file name exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public IllegalFileNameException(String message, Throwable cause) {
    super(message, cause);
  }
}
