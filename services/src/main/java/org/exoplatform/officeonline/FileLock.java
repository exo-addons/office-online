package org.exoplatform.officeonline;

/**
 * The Class FileLock.
 */
public class FileLock {

  /** The lock id. */
  private final String lockId;

  /** The lock token. */
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

  protected long getExpires() {
    return expires;
  }

  protected void setExpires(long expires) {
    this.expires = expires;
  }

  protected String getLockId() {
    return lockId;
  }

  protected String getLockToken() {
    return lockToken;
  }

}
