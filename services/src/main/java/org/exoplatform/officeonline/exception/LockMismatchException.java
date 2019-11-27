package org.exoplatform.officeonline.exception;

public class LockMismatchException extends UpdateConflictException {

  /**
  The Constant serialVersionUID.*/

  private static final long serialVersionUID = -8981933520830552416L;

  /**
   * Instantiates a new LockMismatchException
   *
   * @param message the message
   */
  public LockMismatchException(String message, String lockId) {
    super(message, lockId);
  }
  
  /**
   * Instantiates a new LockMismatchException
   *
   * @param message the message
   */
  public LockMismatchException(String message, String lockId, String filename) {
    super(message, lockId, filename);
  }

}
