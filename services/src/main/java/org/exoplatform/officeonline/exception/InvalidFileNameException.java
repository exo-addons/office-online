package org.exoplatform.officeonline.exception;


/**
 * The Class InvalidFileNameException.
 */
public class InvalidFileNameException extends OfficeOnlineException{

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058617L;


  /**
   * Instantiates a new invalid file name exception.
   *
   * @param message the message
   */
  public InvalidFileNameException(String message) {
    super(message);
  }


  /**
   * Instantiates a new invalid file name exception.
   *
   * @param cause the cause
   */
  public InvalidFileNameException(Throwable cause) {
    super(cause);
  }


  /**
   * Instantiates a new invalid file name exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public InvalidFileNameException(String message, Throwable cause) {
    super(message, cause);
  }
}
