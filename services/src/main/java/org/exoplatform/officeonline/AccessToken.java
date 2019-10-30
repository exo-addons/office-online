package org.exoplatform.officeonline;

import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

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

  /**
   * Return this config as JSON string.
   *
   * @return the string
   * @throws JsonException the json exception
   */
  public String toJSON() throws JsonException {
    JsonGeneratorImpl gen = new JsonGeneratorImpl();
    return gen.createJsonObject(this).toString();
  }

}
