package org.exoplatform.officeonline;

import java.util.Arrays;

import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.cms.link.NodeFinder;
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
                       UserACL userACL,
                       NodeFinder nodeFinder) {
    super(sessionProviders, jcrService, organization, documentService, cacheService, userACL, nodeFinder);
  }


  /**
   * WARNING: ONLY FOR TESTING PURPOSES. SHOULD BE REMOVED ON PRODUCTION
   * 
   * Creates the token with write/rename permissions.
   *
   * @param userId the user id
   * @param fileId the file id
   * @return the string
   * @throws OfficeOnlineException the office online exception
   */
  public String createToken(String userId, String fileId) throws OfficeOnlineException {
    EditorConfig.Builder configBuilder = new EditorConfig.Builder().userId(userId)
                                                                   .fileId(fileId)
                                                                   .workspace("collaboration")
                                                                   .permissions(Arrays.asList(Permissions.USER_CAN_WRITE,
                                                                                              Permissions.USER_CAN_RENAME));
    return generateAccessToken(configBuilder).getToken();
  }

  /**
   * Start.
   */
  @Override
  public void start() {
    LOG.info("Editor Service started");

    // Only for testing purposes
    EditorConfig.Builder configBuilder = new EditorConfig.Builder().userId("peter")
                                                                   .fileId("6514eb3d7f00010146ee6a373b04aa64")
                                                                   .workspace("collaboration")
                                                                   .permissions(Arrays.asList(Permissions.USER_CAN_WRITE,
                                                                                              Permissions.USER_CAN_RENAME));
    try {
      AccessToken accessToken = generateAccessToken(configBuilder);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Access token #1: " + accessToken.getToken());
      }

      // Only for testing purposes
      EditorConfig.Builder configBuilder2 = new EditorConfig.Builder().userId("peter")
                                                                      .fileId("463c327d7f0001012d305152520ad938")
                                                                      .workspace("collaboration")
                                                                      .permissions(Arrays.asList(Permissions.USER_CAN_WRITE));
      AccessToken accessToken2 = generateAccessToken(configBuilder2);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Access token #2: " + accessToken2.getToken());
      }
      EditorConfig decrypted1 = buildEditorConfig(accessToken.getToken());
      EditorConfig decrypted2 = buildEditorConfig(accessToken2.getToken());
      if (LOG.isDebugEnabled()) {
        LOG.debug("DECRYPTED 1: " + decrypted1.getWorkspace() + " " + decrypted1.getUserId() + " " + decrypted1.getFileId() + " "
            + decrypted1.getAccessToken().getExpires());
        decrypted1.getPermissions().forEach(LOG::debug);
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("DECRYPTED 2: " + decrypted2.getWorkspace() + " " + decrypted2.getUserId() + " " + decrypted2.getFileId() + " "
            + decrypted1.getAccessToken().getExpires());
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
