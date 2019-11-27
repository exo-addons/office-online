package org.exoplatform.officeonline.exception;

/**
 * The Class UpdateConflictException.
 */
public class UpdateConflictException extends Exception {

  /**
  The Constant serialVersionUID.*/

  private static final long serialVersionUID = -8981933520830552416L;

  /** The lock id. */
  private String            lockId;

  /** The filename */
  private String            filename;

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
   * Instantiates a new UpdateConflictException.
   *
   * @param message the message
   * @param lockId the lock id
   */
  public UpdateConflictException(String message, String lockId, String filename) {
    super(message);
    this.lockId = lockId;
    this.filename = filename;
  }

  /**
   * Instantiates a new UpdateConflictException.
   *
   * @param message the message
   */
  public UpdateConflictException(String message) {
    super(message);
  }

  /**
   * Gets the lock id.
   *
   * @return the lock id
   */
  public String getLockId() {
    return lockId;
  }
  
  /**
   * Gets the filename.
   *
   * @return the filename
   */
  public String getFileName() {
    return filename;
  }

}
