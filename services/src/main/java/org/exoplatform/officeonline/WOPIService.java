package org.exoplatform.officeonline;

import static org.exoplatform.officeonline.Constants.BASE_FILE_NAME;
import static org.exoplatform.officeonline.Constants.BREADCRUMB_BRAND_NAME;
import static org.exoplatform.officeonline.Constants.BREADCRUMB_BRAND_URL;
import static org.exoplatform.officeonline.Constants.BREADCRUMB_FOLDER_NAME;
import static org.exoplatform.officeonline.Constants.BREADCRUMB_FOLDER_URL;
import static org.exoplatform.officeonline.Constants.CLOSE_URL;
import static org.exoplatform.officeonline.Constants.DOWNLOAD_URL;
import static org.exoplatform.officeonline.Constants.FILE_VERSION_URL;
import static org.exoplatform.officeonline.Constants.HOST_EDIT_URL;
import static org.exoplatform.officeonline.Constants.HOST_VIEW_URL;
import static org.exoplatform.officeonline.Constants.IS_ANONYMOUS_USER;
import static org.exoplatform.officeonline.Constants.LICENSE_CHECK_FOR_EDIT_IS_ENABLED;
import static org.exoplatform.officeonline.Constants.OWNER_ID;
import static org.exoplatform.officeonline.Constants.READ_ONLY;
import static org.exoplatform.officeonline.Constants.SHARE_URL_READ_ONLY;
import static org.exoplatform.officeonline.Constants.SHARE_URL_READ_WRITE;
import static org.exoplatform.officeonline.Constants.SIZE;
import static org.exoplatform.officeonline.Constants.SUPPORTED_SHARE_URL_TYPES;
import static org.exoplatform.officeonline.Constants.SUPPORTS_EXTENDED_LOCK_LENGTH;
import static org.exoplatform.officeonline.Constants.SUPPORTS_GET_LOCK;
import static org.exoplatform.officeonline.Constants.SUPPORTS_LOCKS;
import static org.exoplatform.officeonline.Constants.SUPPORTS_RENAME;
import static org.exoplatform.officeonline.Constants.SUPPORTS_UPDATE;
import static org.exoplatform.officeonline.Constants.USER_CAN_NOT_WRITE_RELATIVE;
import static org.exoplatform.officeonline.Constants.USER_CAN_RENAME;
import static org.exoplatform.officeonline.Constants.USER_CAN_WRITE;
import static org.exoplatform.officeonline.Constants.USER_FRIENDLY_NAME;
import static org.exoplatform.officeonline.Constants.USER_ID;
import static org.exoplatform.officeonline.Constants.VERSION;

import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.officeonline.exception.WopiDiscoveryNotFoundException;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

public class WOPIService extends AbstractOfficeOnlineService implements Startable {

  /** The Constant LOG. */
  protected static final Log    LOG = ExoLogger.getLogger(WOPIService.class);

  /** The discovery plugin. */
  protected WOPIDiscoveryPlugin discoveryPlugin;

  public WOPIService(SessionProviderService sessionProviders,
                     IDGeneratorService idGenerator,
                     RepositoryService jcrService,
                     OrganizationService organization,
                     DocumentService documentService,
                     Authenticator authenticator,
                     IdentityRegistry identityRegistry) {
    super(sessionProviders, idGenerator, jcrService, organization, documentService, authenticator, identityRegistry);
  }

  /**
   * Check file info.
   *
   * @param userSchema the user schema
   * @param userHost the user host
   * @param userPort the user port
   * @param fileId the fileId
   * @param userId the userId
   * @param accessToken the access token
   * @return the map
   * @throws RepositoryException the repository exception
   * @throws OfficeOnlineException the office online exception
   */
  public Map<String, Serializable> checkFileInfo(String userSchema,
                                                 String userHost,
                                                 int userPort,
                                                 String fileId,
                                                 String userId,
                                                 String accessToken) throws RepositoryException, OfficeOnlineException {
    Map<String, Serializable> map = new HashMap<>();
    // remember real context state and session provider to restore them at the end
    ConversationState contextState = ConversationState.getCurrent();
    SessionProvider contextProvider = sessionProviders.getSessionProvider(null);
    try {
      if (!setUserConvoState(userId)) {
        LOG.error("Couldn't set user conversation state. UserId: {}", userId);
        throw new OfficeOnlineException("Cannot set conversation state " + userId);
      }
      // TODO: verify access token
      String workspace = ""; // TODO: get from token
      Node node = nodeByUUID(fileId, workspace);
      addRequiredProperties(map, node);
      addHostCapabilitiesProperties(map);
      addUserMetadataProperties(map);
      addUserPermissionsProperties(map, node);
      addFileURLProperties(map, node, accessToken, userSchema, userHost, userPort);
      addBreadcrumbProperties(map, node, userSchema, userHost, userPort);

    } finally {
      restoreConvoState(contextState, contextProvider);
    }
    return map;
  }

  /**
   * Adds the required properties.
   *
   * @param map the map
   * @param node the node
   * @throws UnsupportedRepositoryOperationException the unsupported repository operation exception
   * @throws RepositoryException the repository exception
   */
  protected void addRequiredProperties(Map<String, Serializable> map, Node node) throws UnsupportedRepositoryOperationException,
                                                                                 RepositoryException {
    map.put(BASE_FILE_NAME, node.getProperty("exo:title").getString());
    map.put(OWNER_ID, node.getProperty("exo:owner").getString());
    map.put(SIZE, getSize(node));
    map.put(USER_ID, WCMCoreUtils.getRemoteUser());
    String version = node.isNodeType("mix:versionable") ? node.getBaseVersion().getName() : "1";
    map.put(VERSION, version);
  }

  /**
   * Adds the host capabilities properties.
   *
   * @param map the map
   */
  protected void addHostCapabilitiesProperties(Map<String, Serializable> map) {
    map.put(SUPPORTS_EXTENDED_LOCK_LENGTH, true);
    map.put(SUPPORTS_GET_LOCK, true);
    map.put(SUPPORTS_LOCKS, true);
    map.put(SUPPORTS_RENAME, true);
    map.put(SUPPORTS_UPDATE, true);
    map.put(SUPPORTED_SHARE_URL_TYPES, (Serializable) Arrays.asList(SHARE_URL_READ_ONLY, SHARE_URL_READ_WRITE));
  }

  /**
   * Adds the user metadata properties.
   *
   * @param map the map
   */
  protected void addUserMetadataProperties(Map<String, Serializable> map) {
    String user = WCMCoreUtils.getRemoteUser();
    User exoUser = getUser(user);
    if (user != null) {
      user = exoUser.getDisplayName();
    }
    map.put(IS_ANONYMOUS_USER, false);
    map.put(LICENSE_CHECK_FOR_EDIT_IS_ENABLED, true);
    map.put(USER_FRIENDLY_NAME, user);
  }

  /**
   * Adds the user permissions properties.
   *
   * @param map the map
   * @param node the node
   * @throws RepositoryException the repository exception
   */
  protected void addUserPermissionsProperties(Map<String, Serializable> map, Node node) throws RepositoryException {
    boolean hasWritePermission = canEditDocument(node);
    map.put(READ_ONLY, !hasWritePermission);
    map.put(USER_CAN_RENAME, hasWritePermission);
    map.put(USER_CAN_WRITE, hasWritePermission);
    // TODO: Check permissions to parent folder
    map.put(USER_CAN_NOT_WRITE_RELATIVE, !hasWritePermission);
  }

  /**
   * Adds the file URL properties.
   *
   * @param map the map
   * @param node the node
   * @param accessToken the access token
   * @param schema the schema
   * @param host the host
   * @param port the port
   * @throws RepositoryException the repository exception
   */
  protected void addFileURLProperties(Map<String, Serializable> map,
                                      Node node,
                                      String accessToken,
                                      String schema,
                                      String host,
                                      int port) throws RepositoryException {
    String explorerLink = explorerLink(node.getPath());
    URI explorerUri = explorerUri(schema, host, port, explorerLink);
    if (explorerUri != null) {
      map.put(CLOSE_URL, explorerUri.toString());
      map.put(FILE_VERSION_URL, explorerUri.toString());
    }
    StringBuilder platformUrl = platformUrl(schema, host, port);
    String platformRestURL = new StringBuilder(platformUrl).append('/')
                                                           .append(PortalContainer.getCurrentRestContextName())
                                                           .toString();

    String downloadURL = new StringBuilder(platformRestURL).append("/officeonline/editor/content/")
                                                           .append(node.getUUID())
                                                           .append("/")
                                                           .append(WCMCoreUtils.getRemoteUser())
                                                           .append("?access_token=")
                                                           .append(accessToken)
                                                           .toString();
    map.put(DOWNLOAD_URL, downloadURL);
    // TODO: set url to the portlet
    map.put(HOST_EDIT_URL, null);
    map.put(HOST_VIEW_URL, null);

  }

  /**
   * Adds the breadcrumb properties.
   *
   * @param map the map
   * @param node the node
   * @param schema the schema
   * @param host the host
   * @param port the port
   */
  protected void addBreadcrumbProperties(Map<String, Serializable> map, Node node, String schema, String host, int port) {
    // TODO: replace by real values
    map.put(BREADCRUMB_BRAND_NAME, "ExoPlatform");
    map.put(BREADCRUMB_BRAND_URL, "exoplatform.com");
    try {
      Node parent = node.getParent();
      String url = explorerUri(schema, host, port, explorerLink(parent.getPath())).toString();
      map.put(BREADCRUMB_FOLDER_NAME, parent.getProperty("exo:title").getString());
      map.put(BREADCRUMB_FOLDER_URL, url);
    } catch (Exception e) {
      LOG.error("Couldn't add breadcrump properties:", e);
    }

  }

  /**
   * Verify proof key.
   *
   * @param proofKeyHeader the proof key header
   * @param oldProofKeyHeader the old proof key header
   * @param url the url
   * @param accessToken the access token
   * @param timestampHeader the timestamp header
   * @return true, if successful
   */
  public boolean verifyProofKey(String proofKeyHeader,
                                String oldProofKeyHeader,
                                String url,
                                String accessToken,
                                String timestampHeader) {
    if (StringUtils.isBlank(proofKeyHeader)) {
      return true; // assume valid
    }

    long timestamp = Long.parseLong(timestampHeader);
    if (!ProofKeyHelper.verifyTimestamp(timestamp)) {
      return false;
    }

    byte[] expectedProofBytes = ProofKeyHelper.getExpectedProofBytes(url, accessToken, timestamp);
    // follow flow from https://wopi.readthedocs.io/en/latest/scenarios/proofkeys.html#verifying-the-proof-keys
    boolean res = ProofKeyHelper.verifyProofKey(discoveryPlugin.getProofKey(), proofKeyHeader, expectedProofBytes);
    if (!res && StringUtils.isNotBlank(oldProofKeyHeader)) {
      res = ProofKeyHelper.verifyProofKey(discoveryPlugin.getProofKey(), oldProofKeyHeader, expectedProofBytes);
      if (!res) {
        res = ProofKeyHelper.verifyProofKey(discoveryPlugin.getOldProofKey(), proofKeyHeader, expectedProofBytes);
      }
    }
    return res;
  }

  /**
   * Sets the plugin.
   *
   * @param plugin the plugin
   */
  public void setWOPIDiscoveryPlugin(ComponentPlugin plugin) {
    Class<WOPIDiscoveryPlugin> pclass = WOPIDiscoveryPlugin.class;
    if (pclass.isAssignableFrom(plugin.getClass())) {
      discoveryPlugin = pclass.cast(plugin);
      LOG.info("Set WopiDiscoveryPlugin instance of " + plugin.getClass().getName());
    } else {
      throw new WopiDiscoveryNotFoundException("WopiDiscoveryPlugin is not an instance of " + pclass.getName());
    }
  }

  @Override
  public void start() {
    if (discoveryPlugin == null) {
      throw new WopiDiscoveryNotFoundException("WopiDiscoveryPlugin is not configured");
    }
    discoveryPlugin.start();

    LOG.debug("Office Online Editor Service started");
    // Only for testing purposes
    String excelEdit = discoveryPlugin.getActionUrl("xlsx", "edit");
    String excelView = discoveryPlugin.getActionUrl("xlsx", "view");
    String wordEdit = discoveryPlugin.getActionUrl("docx", "edit");
    String wordView = discoveryPlugin.getActionUrl("docx", "view");
    String powerPointEdit = discoveryPlugin.getActionUrl("pptx", "edit");
    String powerPointView = discoveryPlugin.getActionUrl("pptx", "view");

    LOG.debug("Excel edit URL: " + excelEdit);
    LOG.debug("Excel view URL: " + excelView);
    LOG.debug("Word edit URL: " + wordEdit);
    LOG.debug("Excel view URL: " + wordView);
    LOG.debug("PowerPoint edit URL: " + powerPointEdit);
    LOG.debug("PowerPoint view URL: " + powerPointView);
  }

  /**
   * Stop.
   */
  @Override
  public void stop() {
    discoveryPlugin.stop();

  }
}
