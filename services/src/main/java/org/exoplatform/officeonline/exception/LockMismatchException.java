package org.exoplatform.officeonline.exception;

/**
 * The Class LockMismatchException.
 */
public class LockMismatchException extends UpdateConflictException {

  /**
  The Constant serialVersionUID.*/

  private static final long serialVersionUID = -8981933520830552416L;

  /**
   * Instantiates a new LockMismatchException.
   *
   * @param message the message
   * @param lockId the lock id
   */
  public LockMismatchException(String message, String lockId) {
    super(message, lockId);
  }
  
  /**
   * Instantiates a new LockMismatchException.
   *
   * @param message the message
   * @param lockId the lock id
   * @param filename the filename
   */
  public LockMismatchException(String message, String lockId, String filename) {
    super(message, lockId, filename);
  }

}
