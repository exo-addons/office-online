package org.exoplatform.officeonline;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.io.input.AutoCloseInputStream;

import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityRegistry;

public class EditorService extends AbstractOfficeOnlineService {

  /** The Constant LOG. */
  protected static final Log LOG = ExoLogger.getLogger(EditorService.class);

  public EditorService(SessionProviderService sessionProviders,
                       IDGeneratorService idGenerator,
                       RepositoryService jcrService,
                       OrganizationService organization,
                       DocumentService documentService,
                       Authenticator authenticator,
                       IdentityRegistry identityRegistry) {
    super(sessionProviders, idGenerator, jcrService, organization, documentService, authenticator, identityRegistry);
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
  // TODO: return only token
  public EditorConfig createEditorConfig(String userSchema,
                                         String userHost,
                                         int userPort,
                                         String userId,
                                         String workspace,
                                         String fileId) throws RepositoryException, OfficeOnlineException {
    EditorConfig config = null;
    // remember real context state and session provider to restore them at the end
    ConversationState contextState = ConversationState.getCurrent();
    SessionProvider contextProvider = sessionProviders.getSessionProvider(null);
    try {
      if (!setUserConvoState(userId)) {
        LOG.error("Couldn't set user conversation state. UserId: {}", userId);
        throw new OfficeOnlineException("Cannot set conversation state " + userId);
      }

      Node document = nodeByUUID(workspace, fileId);
      List<Permissions> permissions = new ArrayList<>();

      if (document != null) {
        if (canEditDocument(document)) {
          permissions.add(Permissions.USER_CAN_WRITE);
          permissions.add(Permissions.USER_CAN_RENAME);
        } else {
          permissions.add(Permissions.READ_ONLY);
        }
      }

      config = new EditorConfig(userId, fileId, workspace, permissions);
      String accessToken = generateAccessToken(config);
      config.setAccessToken(accessToken);
    } finally {
      restoreConvoState(contextState, contextProvider);
    }
    return config;
  }

  protected String generateAccessToken(EditorConfig config) {
    
    return null;
  }

  /**
   * Gets the content.
   *
   * @param userId the user id
   * @param fileId the file id
   * @param accessToken the access token
   * @return the content
   * @throws OfficeOnlineException the office online exception
   */
  public DocumentContent getContent(String userId, String fileId, String accessToken) throws OfficeOnlineException {
    // TODO: verify accessToken
    String workspace = ""; // TODO: get from the token
    ConversationState contextState = ConversationState.getCurrent();
    SessionProvider contextProvider = sessionProviders.getSessionProvider(null);
    try {
      // We all the job under actual (requester) user here
      if (!setUserConvoState(userId)) {
        LOG.error("Couldn't set user conversation state. UserId: {}", userId);
        throw new OfficeOnlineException("Cannot set conversation state " + userId);
      }
      // work in user session
      Node node = nodeByUUID(fileId, workspace);
      if (node == null) {
        throw new OfficeOnlineException("File not found. fileId: " + fileId);
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
      LOG.error("Cannot get content of node. FileId: " + fileId, e.getMessage());
      throw new OfficeOnlineException("Cannot get file content");
    } finally {
      restoreConvoState(contextState, contextProvider);
    }

  }

  /**
   * Start.
   */
  @Override
  public void start() {
    // TODO Auto-generated method stub
  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub
  }

}
