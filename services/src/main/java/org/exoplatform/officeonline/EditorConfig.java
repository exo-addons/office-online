package org.exoplatform.officeonline;

import java.util.ArrayList;
import java.util.List;

/**
 * EditorConfig represents single user-permissions-file combination.
 */
public class EditorConfig {

  /** The access token. */
  protected String       accessToken;

  /** The user id. */
  protected String       userId;

  /** The file id. */
  protected String       fileId;

  /** The permissions. */
  protected List<String> permissions = new ArrayList<>();

  /**
   * Instantiates a new editor config.
   *
   * @param userId the user id
   * @param fileId the file id
   * @param permissions the permissions
   */
  public EditorConfig(String userId, String fileId, List<String> permissions) {
    this.userId = userId;
    this.fileId = fileId;
    this.permissions = permissions;
  }

  /**
   * Gets the user id.
   *
   * @return the user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user id.
   *
   * @param userId the new user id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Gets the file id.
   *
   * @return the file id
   */
  public String getFileId() {
    return fileId;
  }

  /**
   * Sets the file id.
   *
   * @param fileId the new file id
   */
  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  /**
   * Gets the permissions.
   *
   * @return the permissions
   */
  public List<String> getPermissions() {
    return permissions;
  }

  /**
   * Sets the permissions.
   *
   * @param permissions the permissions
   */
  public void setPermissions(List<String> permissions) {
    this.permissions = permissions;
  }

  /**
   * Gets the access token.
   *
   * @return the access token
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * Sets the access token.
   *
   * @param accessToken the new access token
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
  

}
