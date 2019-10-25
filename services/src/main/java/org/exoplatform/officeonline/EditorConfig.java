package org.exoplatform.officeonline;

import java.util.ArrayList;
import java.util.List;

/**
 * EditorConfig represents single user-permissions-file combination.
 */
public class EditorConfig {

  /** The access token. */
  protected String            accessToken;

  /** The user id. */
  protected String            userId;

  /** The file id. */
  protected String            fileId;

  /** The workspace. */
  protected String            workspace;

  /** The permissions. */
  protected List<Permissions> permissions = new ArrayList<>();


  /**
   * Instantiates a new editor config.
   *
   * @param userId the user id
   * @param fileId the file id
   * @param workspace the workspace
   * @param permissions the permissions
   */
  public EditorConfig(String userId, String fileId, String workspace, List<Permissions> permissions) {
    this.userId = userId;
    this.fileId = fileId;
    this.workspace = workspace;
    this.permissions = permissions;
  }
  
  /**
   * Instantiates a new editor config.
   *
   * @param userId the user id
   * @param fileId the file id
   * @param workspace the workspace
   * @param permissions the permissions
   * @param accessToken the access token
   */
  public EditorConfig(String userId, String fileId, String workspace, List<Permissions> permissions, String accessToken) {
    this.userId = userId;
    this.fileId = fileId;
    this.workspace = workspace;
    this.permissions = permissions;
    this.accessToken = accessToken;
  }

  /**
   * Gets the user id.
   *
   * @return the user id
   */
  protected String getUserId() {
    return userId;
  }

  /**
   * Gets the file id.
   *
   * @return the file id
   */
  protected String getFileId() {
    return fileId;
  }

  /**
   * Gets the permissions.
   *
   * @return the permissions
   */
  protected List<Permissions> getPermissions() {
    return permissions;
  }

  /**
   * Gets the access token.
   *
   * @return the access token
   */
  protected String getAccessToken() {
    return accessToken;
  }

  /**
   * Sets the access token.
   *
   * @param accessToken the new access token
   */
  protected void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   * Gets the workspace.
   *
   * @return the workspace
   */
  protected String getWorkspace() {
    return workspace;
  }
  
}
