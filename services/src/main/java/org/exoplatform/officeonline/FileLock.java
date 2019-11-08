package org.exoplatform.officeonline;

/**
 * The Class FileLock.
 */
public class FileLock {

  /**  The fileId. */
  private final String fileId;

  /** The lock id. */
  private final String lockId;

  /** The lock token. */
  private final String lockToken;

  /** The expires. */
  private long         expires;

  /**
   * Instantiates a new file lock.
   *
   * @param fileId the file id
   * @param lockId the lock id
   * @param lockToken the lock token
   * @param expires the expires
   */
  protected FileLock(String fileId, String lockId, String lockToken, long expires) {
    this.fileId = fileId;
    this.lockId = lockId;
    this.lockToken = lockToken;
    this.expires = expires;
  }

  /**
   * Gets the expires.
   *
   * @return the expires
   */
  protected long getExpires() {
    return expires;
  }

  /**
   * Sets the expires.
   *
   * @param expires the new expires
   */
  protected void setExpires(long expires) {
    this.expires = expires;
  }

  /**
   * Gets the lock id.
   *
   * @return the lock id
   */
  protected String getLockId() {
    return lockId;
  }

  /**
   * Gets the lock token.
   *
   * @return the lock token
   */
  protected String getLockToken() {
    return lockToken;
  }

  /**
   * Gets the file id.
   *
   * @return the file id
   */
  protected String getFileId() {
    return fileId;
  }

}
