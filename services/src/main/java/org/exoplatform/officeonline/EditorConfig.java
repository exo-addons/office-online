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

  /** The permissions. */
  protected final List<Permissions> permissions;

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

  public static class Builder {

    /** The access token. */
    private AccessToken       accessToken;

    /** The user id. */
    private String            userId;

    /** The file id. */
    private String            fileId;

    /** The workspace. */
    private String            workspace;

    /** The permissions. */
    private List<Permissions> permissions = new ArrayList<>();

    protected Builder accessToken(AccessToken accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    protected Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    protected Builder fileId(String fileId) {
      this.fileId = fileId;
      return this;
    }

    protected Builder workspace(String workspace) {
      this.workspace = workspace;
      return this;
    }

    protected Builder permissions(List<Permissions> permissions) {
      this.permissions = permissions;
      return this;
    }

    protected String userId() {
      return userId;
    }

    protected String fileId() {
      return fileId;
    }

    protected String workspace() {
      return workspace;
    }

    protected List<Permissions> permissions() {
      return permissions;
    }

    protected AccessToken accessToken() {
      return accessToken;
    }

    protected EditorConfig build() {
      return new EditorConfig(userId, fileId, workspace, permissions, accessToken);
    }

  }

}
