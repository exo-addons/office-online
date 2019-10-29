package org.exoplatform.officeonline;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

/**
 * EditorConfig represents single user-permissions-file combination.
 */
public class EditorConfig {

  /** The access token. */
  protected AccessToken       accessToken;

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
   * @param tokenExpires the token ttl
   */
  public EditorConfig(String userId, String fileId, String workspace, List<Permissions> permissions, AccessToken accessToken) {
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
  public AccessToken getAccessToken() {
    return accessToken;
  }

  /**
   * Sets the access token.
   *
   * @param accessToken the new access token
   */
  protected void setAccessToken(AccessToken accessToken) {
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
