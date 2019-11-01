package org.exoplatform.officeonline.exception;

/**
 * The Class FileNotFoundException.
 */
public class FileNotFoundException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058618L;

  
  public FileNotFoundException(String message) {
    super(message);
  }

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param cause the cause
   */
  public FileNotFoundException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public FileNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
