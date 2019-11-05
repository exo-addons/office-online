package org.exoplatform.officeonline.exception;

/**
 * The Class AuthenticationFailedException.
 */
public class AuthenticationFailedException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058617L;


  /**
   * Instantiates a new authentication failed exception.
   *
   * @param message the message
   */
  public AuthenticationFailedException(String message) {
    super(message);
  }

  /**
   * Instantiates a new authentication failed exception.
   *
   * @param cause the cause
   */
  public AuthenticationFailedException(Throwable cause) {
    super(cause);
  }


  /**
   * Instantiates a new authentication failed exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public AuthenticationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
