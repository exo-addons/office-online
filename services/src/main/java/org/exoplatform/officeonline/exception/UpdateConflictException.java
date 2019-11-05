package org.exoplatform.officeonline.exception;


/**
 * The Class UpdateConflictException.
 */
public abstract class UpdateConflictException extends Exception {

  /**
  The Constant serialVersionUID.*/

  private static final long serialVersionUID = -8981933520830552416L;

  /** The lock id. */
  private String            lockId;

  /**
   * Instantiates a new UpdateConflictException.
   *
   * @param message the message
   * @param lockId the lock id
   */
  public UpdateConflictException(String message, String lockId) {
    super(message);
    this.lockId = lockId;
  }

  /**
   * Gets the lock id.
   *
   * @return the lock id
   */
  public String getLockId() {
    return lockId;
  }

}
