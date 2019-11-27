package org.exoplatform.officeonline.exception;


/**
 * The Class FileLockedException.
 */
public class FileLockedException extends LockMismatchException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058617L;

  /**
   * Instantiates a new file locked exception.
   *
   * @param message the message
   * @param lockId the lock id
   */
  public FileLockedException(String message, String lockId) {
    super(message, lockId);
  }
  
  /**
   * Instantiates a new file locked exception.
   *
   * @param message the message
   * @param lockId the lock id
   */
  public FileLockedException(String message, String lockId, String filename) {
    super(message, lockId, filename);
  }

}
