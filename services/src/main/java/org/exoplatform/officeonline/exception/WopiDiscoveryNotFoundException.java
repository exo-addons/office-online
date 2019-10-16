package org.exoplatform.officeonline.exception;

/**
 * The Class WopiDiscoveryNotFoundException.
 */
public class WopiDiscoveryNotFoundException extends RuntimeException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = -2809227082503353128L;

  /**
   * Instantiates a new wopi discovery not found exception.
   *
   * @param message the message
   */
  public WopiDiscoveryNotFoundException(String message) {
    super(message);
  }

  /**
   * Instantiates a new wopi discovery not found exception.
   *
   * @param cause the cause
   */
  public WopiDiscoveryNotFoundException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new wopi discovery not found exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public WopiDiscoveryNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
