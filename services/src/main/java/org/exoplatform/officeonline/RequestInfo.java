/*
 * 
 */
package org.exoplatform.officeonline;

import java.util.Locale;

/**
 * The Class RequestInfo.
 */
public class RequestInfo {

  /** The scheme. */
  protected String scheme;

  /** The server name. */
  protected String serverName;

  /** The port. */
  protected int    port;

  /** The remote user. */
  protected String remoteUser;

  /** The locale. */
  protected Locale locale;

  /**
   * Instantiates a new request info.
   *
   * @param scheme the scheme
   * @param serverName the server name
   * @param port the port
   * @param remoteUser the remote user
   */
  public RequestInfo(String scheme, String serverName, int port, String remoteUser) {
    this.scheme = scheme;
    this.serverName = serverName;
    this.port = port;
    this.remoteUser = remoteUser;
  }

  /**
   * Instantiates a new request info.
   *
   * @param scheme the scheme
   * @param serverName the server name
   * @param port the port
   * @param locale the locale
   */
  public RequestInfo(String scheme, String serverName, int port, String remoteUser, Locale locale) {
    this.scheme = scheme;
    this.serverName = serverName;
    this.port = port;
    this.remoteUser = remoteUser;
    this.locale = locale;
  }

  /**
   * Gets the scheme.
   *
   * @return the scheme
   */
  public String getScheme() {
    return scheme;
  }

  /**
   * Gets the server name.
   *
   * @return the server name
   */
  public String getServerName() {
    return serverName;
  }

  /**
   * Gets the port.
   *
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * Gets the remote user.
   *
   * @return the remote user
   */
  public String getRemoteUser() {
    return remoteUser;
  }

  /**
   * Gets the locale.
   *
   * @return the locale
   */
  public Locale getLocale() {
    return locale;
  }

}
