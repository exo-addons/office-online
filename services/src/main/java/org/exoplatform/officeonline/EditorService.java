package org.exoplatform.officeonline;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.input.AutoCloseInputStream;

import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;

/**
 * The Class EditorService.
 */
public class EditorService extends AbstractOfficeOnlineService {

  /** The Constant LOG. */
  protected static final Log LOG = ExoLogger.getLogger(EditorService.class);

  /**
   * Instantiates a new editor service.
   *
   * @param sessionProviders the session providers
   * @param jcrService the jcr service
   * @param organization the organization
   * @param documentService the document service
   * @param cacheService the cache service
   * @param userACL the user ACL
   */
  public EditorService(SessionProviderService sessionProviders,
                       RepositoryService jcrService,
                       OrganizationService organization,
                       DocumentService documentService,
                       CacheService cacheService,
                       UserACL userACL) {
    super(sessionProviders, jcrService, organization, documentService, cacheService, userACL);
  }

  /**
   * Creates the editor config.
   *
   * @param userSchema the user schema
   * @param userHost the user host
   * @param userPort the user port
   * @param userId the user id
   * @param workspace the workspace
   * @param fileId the file id
   * @return the editor config
   * @throws RepositoryException the repository exception
   * @throws OfficeOnlineException the office online exception
   */
  public EditorConfig createEditorConfig(String userSchema,
                                         String userHost,
                                         int userPort,
                                         String userId,
                                         String workspace,
                                         String fileId) throws RepositoryException, OfficeOnlineException {

    Node document = nodeByUUID(fileId, workspace);
    List<Permissions> permissions = new ArrayList<>();

    if (document != null) {
      if (canEditDocument(document)) {
        permissions.add(Permissions.USER_CAN_WRITE);
        permissions.add(Permissions.USER_CAN_RENAME);
      } else {
        permissions.add(Permissions.READ_ONLY);
      }
    }
    EditorConfig config = new EditorConfig(userId, fileId, workspace, permissions);
    String accessToken = generateAccessToken(config);
    config.setAccessToken(accessToken);
    return config;
  }

  /**
   * Gets the content.
   *
   * @param fileId the fileId
   * @param config the config
   * @return the content
   * @throws OfficeOnlineException the office online exception
   */
  public DocumentContent getContent(String fileId, EditorConfig config) throws OfficeOnlineException {
    if (config == null) {
      throw new OfficeOnlineException("Cannot getContent. Config is null.");
    }
    if (!fileId.equals(config.getFileId())) {
      throw new OfficeOnlineException("FileId from request doesn't match config.fileId");
    }

    try {
      Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
      if (node == null) {
        throw new OfficeOnlineException("File not found. fileId: " + config.getFileId());
      }
      Node content = nodeContent(node);

      final String mimeType = content.getProperty("jcr:mimeType").getString();
      // data stream will be closed when EoF will be reached
      final InputStream data = new AutoCloseInputStream(content.getProperty("jcr:data").getStream());
      return new DocumentContent() {
        @Override
        public String getType() {
          return mimeType;
        }

        @Override
        public InputStream getData() {
          return data;
        }
      };
    } catch (RepositoryException e) {
      LOG.error("Cannot get content of node. FileId: " + config.getFileId(), e.getMessage());
      throw new OfficeOnlineException("Cannot get file content. FileId: " + config.getFileId());
    }

  }

  /**
   * Start.
   */
  @Override
  public void start() {
    LOG.info("Editor Service started");

    // Only for testing purposes
    EditorConfig config = new EditorConfig("vlad",
                                           "93268635624323427",
                                           "collaboration",
                                           Arrays.asList(Permissions.USER_CAN_WRITE, Permissions.USER_CAN_RENAME));
    try {
      String accessToken = generateAccessToken(config);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Access token #1: " + accessToken);
      }

      EditorConfig config2 = new EditorConfig("peter", "09372697", "private", new ArrayList<Permissions>());
      String accessToken2 = generateAccessToken(config2);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Access token #2: " + accessToken2);
      }
      EditorConfig decrypted1 = buildEditorConfig(accessToken);
      EditorConfig decrypted2 = buildEditorConfig(accessToken2);
      if (LOG.isDebugEnabled()) {
        LOG.debug("DECRYPTED 1: " + decrypted1.getWorkspace() + " " + decrypted1.getUserId() + " " + decrypted1.getFileId());
        decrypted1.getPermissions().forEach(LOG::debug);
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("DECRYPTED 2: " + decrypted2.getWorkspace() + " " + decrypted2.getUserId() + " " + decrypted2.getFileId());
      }
      decrypted2.getPermissions().forEach(LOG::debug);
    } catch (OfficeOnlineException e) {
      LOG.error(e);
    }

  }

  /**
   * Stop.
   */
  @Override
  public void stop() {
    // TODO Auto-generated method stub
  }

}
