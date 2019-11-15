package org.exoplatform.officeonline.exception;

/**
 * The Class ProofKeyValidationException.
 */
public class ProofKeyValidationException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058617L;


  /**
   * Instantiates a new proof key validation exception.
   *
   * @param message the message
   */
  public ProofKeyValidationException(String message) {
    super(message);
  }


  /**
   * Instantiates a new proof key validation exception.
   *
   * @param cause the cause
   */
  public ProofKeyValidationException(Throwable cause) {
    super(cause);
  }


  /**
   * Instantiates a new proof key validation exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public ProofKeyValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
