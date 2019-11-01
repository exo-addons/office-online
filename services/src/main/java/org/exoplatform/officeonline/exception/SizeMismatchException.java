package org.exoplatform.officeonline.exception;


/**
 * The Class SizeMismatchException.
 */
public class SizeMismatchException extends UpdateConflictException {

  /**
  The Constant serialVersionUID.*/

  private static final long serialVersionUID = -8981933520830552416L;

  /**
   * Instantiates a new SizeMismatchException.
   *
   * @param message the message
   * @param lockId the lock id
   */
  public SizeMismatchException(String message, String lockId) {
    super(message, lockId);
  }

}
