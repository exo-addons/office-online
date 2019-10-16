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
  
  /** The workspace. */
  protected String workspace;

  /** The permissions. */
  protected List<String> permissions = new ArrayList<>();

  /**
   * Instantiates a new editor config.
   *
   * @param userId the user id
   * @param fileId the file id
   * @param workspace the workspace
   * @param permissions the permissions
   */
  public EditorConfig(String userId, String fileId, String workspace, List<String> permissions) {
    this.userId = userId;
    this.fileId = fileId;
    this.workspace = workspace;
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
   * Gets the file id.
   *
   * @return the file id
   */
  public String getFileId() {
    return fileId;
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

  /**
   * Gets the workspace.
   *
   * @return the workspace
   */
  public String getWorkspace() {
    return workspace;
  }

}
