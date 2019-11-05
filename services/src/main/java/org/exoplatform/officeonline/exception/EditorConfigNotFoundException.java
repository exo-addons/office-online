package org.exoplatform.officeonline.exception;


/**
 * The Class ConfigNotFoundException.
 */
public class EditorConfigNotFoundException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058617L;


  /**
   * Instantiates a new config not found exception.
   *
   * @param message the message
   */
  public EditorConfigNotFoundException(String message) {
    super(message);
  }


  /**
   * Instantiates a new config not found exception.
   *
   * @param cause the cause
   */
  public EditorConfigNotFoundException(Throwable cause) {
    super(cause);
  }


  /**
   * Instantiates a new config not found exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EditorConfigNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
