package org.exoplatform.officeonline.exception;

/**
 * The Class PermissionDeniedException.
 */
public class PermissionDeniedException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058617L;


  /**
   * Instantiates a new permission denied exception.
   *
   * @param message the message
   */
  public PermissionDeniedException(String message) {
    super(message);
  }

  /**
   * Instantiates a new permission denied exception.
   *
   * @param cause the cause
   */
  public PermissionDeniedException(Throwable cause) {
    super(cause);
  }


  /**
   * Instantiates a new permission denied exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public PermissionDeniedException(String message, Throwable cause) {
    super(message, cause);
  }
}
