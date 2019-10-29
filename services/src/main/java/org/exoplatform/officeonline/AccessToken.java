package org.exoplatform.officeonline;


/**
 * The Class AccessToken.
 */
public class AccessToken {

  /** The token. */
  private String token;

  /** The expires. */
  private long   expires;

  /**
   * Instantiates a new access token.
   *
   * @param token the token
   * @param expires the expires
   */
  public AccessToken(String token, long expires) {
    super();
    this.token = token;
    this.expires = expires;
  }

  /**
   * Gets the token.
   *
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * Gets the expires.
   *
   * @return the expires
   */
  public long getExpires() {
    return expires;
  }

}
