package org.exoplatform.officeonline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * EditorConfig represents single user-permissions-file combination.
 */
public class EditorConfig {

  /** The access token. */
  protected final AccessToken       accessToken;

  /** The user id. */
  protected final String            userId;

  /** The file id. */
  protected final String            fileId;

  /** The workspace. */
  protected final String            workspace;

  /** The base url. */
  protected final String            baseUrl;

  /** The permissions. */
  protected final List<Permissions> permissions;

  /**
   * Instantiates a new editor config.
   *
   * @param userId the user id
   * @param fileId the file id
   * @param workspace the workspace
   * @param baseUrl the platform url
   * @param permissions the permissions
   * @param accessToken the access token
   */
  public EditorConfig(String userId,
                      String fileId,
                      String workspace,
                      String baseUrl,
                      List<Permissions> permissions,
                      AccessToken accessToken) {
    this.userId = userId;
    this.fileId = fileId;
    this.workspace = workspace;
    this.baseUrl = baseUrl;
    this.permissions = permissions != null ? permissions : Collections.emptyList();
    this.accessToken = accessToken;
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
   * Gets the workspace.
   *
   * @return the workspace
   */
  public String getWorkspace() {
    return workspace;
  }

  /**
   * Gets the base url.
   *
   * @return the workspace
   */
  public String getBaseUrl() {
    return baseUrl;
  }

  public static class Builder {

    /** The access token. */
    private AccessToken       accessToken;

    /** The user id. */
    private String            userId;

    /** The file id. */
    private String            fileId;

    /** The workspace. */
    private String            workspace;

    /** The base url. */
    private String            baseUrl;

    /** The permissions. */
    private List<Permissions> permissions = new ArrayList<>();

    public Builder accessToken(AccessToken accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder fileId(String fileId) {
      this.fileId = fileId;
      return this;
    }

    public Builder workspace(String workspace) {
      this.workspace = workspace;
      return this;
    }

    public Builder baseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
      return this;
    }

    public Builder permissions(List<Permissions> permissions) {
      this.permissions = permissions;
      return this;
    }

    public String userId() {
      return userId;
    }

    public String fileId() {
      return fileId;
    }

    public String workspace() {
      return workspace;
    }

    public String baseUrl() {
      return baseUrl;
    }

    public List<Permissions> permissions() {
      return permissions;
    }

    public AccessToken accessToken() {
      return accessToken;
    }

    public EditorConfig build() {
      return new EditorConfig(userId, fileId, workspace, baseUrl, permissions, accessToken);
    }

  }

}
