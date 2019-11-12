package org.exoplatform.officeonline;

/**
 * The Class FileLock.
 */
public class FileLock {

  /** The external lock id that used by WOPI clients */
  private final String lockId;

  /** The JCR lock token. */
  private final String lockToken;

  /** The expires. */
  private long         expires;

  /**
   * Instantiates a new file lock.
   *
   * @param lockId the lock id
   * @param lockToken the lock token
   * @param expires the expires
   */
  protected FileLock(String lockId, String lockToken, long expires) {
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

}
