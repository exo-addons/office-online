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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * The Class OfficeOnlineEditorServiceImpl.
 */
public class OfficeOnlineEditorService implements Startable {

  /** The Constant LOG. */
  protected static final Log             LOG     = ExoLogger.getLogger(OfficeOnlineEditorService.class);

  /** The generator. */
  protected final IDGeneratorService     idGenerator;

  /** The session providers. */
  protected final SessionProviderService sessionProviders;

  /** The jcr service. */
  protected final RepositoryService      jcrService;

  /** The organization. */
  protected final OrganizationService    organization;

  /** The document service. */
  protected final DocumentService        documentService;

  /** The discovery plugin. */
  protected WOPIDiscoveryPlugin          discoveryPlugin;

  /** The configs. */
  protected Map<String, EditorConfig>    configs = new ConcurrentHashMap<String, EditorConfig>();

  /**
   * Instantiates a new office online editor service.
   *
   * @param sessionProviders the session providers
   * @param idGenerator the id generator
   * @param jcrService the jcr service
   * @param organization the organization
   * @param documentService the document service
   */
  public OfficeOnlineEditorService(SessionProviderService sessionProviders,
                                   IDGeneratorService idGenerator,
                                   RepositoryService jcrService,
                                   OrganizationService organization,
                                   DocumentService documentService) {
    this.sessionProviders = sessionProviders;
    this.idGenerator = idGenerator;
    this.jcrService = jcrService;
    this.organization = organization;
    this.documentService = documentService;
  }

  /**
   * Start.
   */
  @Override
  public void start() {
    if (discoveryPlugin == null) {
      throw new RuntimeException("WopiDiscoveryPlugin is not configured");
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
   * Check file info.
   *
   * @param userSchema the user schema
   * @param userHost the user host
   * @param userPort the user port
   * @param workspace the workspace
   * @param fileId the file id
   * @param accessToken the access token
   * @return the map
   * @throws RepositoryException the repository exception
   */
  public Map<String, Serializable> checkFileInfo(String userSchema,
                                                 String userHost,
                                                 int userPort,
                                                 String workspace,
                                                 String fileId,
                                                 String accessToken) throws RepositoryException {
    Node node = nodeByUUID(fileId, workspace);
    Map<String, Serializable> map = new HashMap<>();
    addRequiredProperties(map, node);
    addHostCapabilitiesProperties(map);
    addUserMetadataProperties(map);
    addUserPermissionsProperties(map, node);
    addFileURLProperties(map, node, accessToken, userSchema, userHost, userPort);
    addBreadcrumbProperties(map, node, userSchema, userHost, userPort);
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

    // TODO: implement REST endpoint
    String downloadURL = new StringBuilder(platformRestURL).append("/officeonline/editor/content/")
                                                           .append(WCMCoreUtils.getRemoteUser())
                                                           .append("/")
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
    }
    catch (Exception e) {
      LOG.error("Couldn't add breadcrump properties:", e);
    }
  

  }

  /**
   * Platform url.
   *
   * @param schema the schema
   * @param host the host
   * @param port the port
   * @return the string builder
   */
  protected StringBuilder platformUrl(String schema, String host, int port) {
    StringBuilder platformUrl = new StringBuilder();
    platformUrl.append(schema);
    platformUrl.append("://");
    platformUrl.append(host);
    if (port >= 0 && port != 80 && port != 443) {
      platformUrl.append(':');
      platformUrl.append(port);
    }
    platformUrl.append('/');
    platformUrl.append(PortalContainer.getCurrentPortalContainerName());

    return platformUrl;
  }

  /**
   * Explorer uri.
   *
   * @param schema the schema
   * @param host the host
   * @param port the port
   * @param ecmsLink the ecms link
   * @return the uri
   */
  protected URI explorerUri(String schema, String host, int port, String ecmsLink) {
    URI uri;
    try {
      ecmsLink = URLDecoder.decode(ecmsLink, StandardCharsets.UTF_8.name());
      String[] linkParts = ecmsLink.split("\\?");
      if (linkParts.length >= 2) {
        uri = new URI(schema, null, host, port, linkParts[0], linkParts[1], null);
      } else {
        uri = new URI(schema, null, host, port, ecmsLink, null, null);
      }
    } catch (Exception e) {
      LOG.warn("Error creating document URI", e);
      try {
        uri = URI.create(ecmsLink);
      } catch (Exception e1) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Error creating document URI from ECMS link and after error: " + e.getMessage(), e1);
        }
        uri = null;
      }
    }
    return uri;
  }

  /**
   * ECMS explorer page relative URL (within the Platform).
   *
   * @param jcrPath the jcr path
   * @return the string
   */
  protected String explorerLink(String jcrPath) {
    try {
      return documentService.getLinkInDocumentsApp(jcrPath);
    } catch (Exception e) {
      LOG.warn("Error creating document link for " + jcrPath, e);
      return new StringBuilder().append('/').append(PortalContainer.getCurrentPortalContainerName()).toString();
    }
  }

  /**
   * Gets the size.
   *
   * @param node the node
   * @return the size
   */
  protected Long getSize(Node node) {
    long size = 0;
    try {
      if (node.isNodeType(NodetypeConstant.EXO_SYMLINK)) {
        node = Utils.getNodeSymLink(node);
      }
      if (node.hasNode(Utils.JCR_CONTENT)) {
        Node contentNode = node.getNode(Utils.JCR_CONTENT);
        if (contentNode.hasProperty(Utils.JCR_DATA)) {
          size = contentNode.getProperty(Utils.JCR_DATA).getLength();
        }
      }
    } catch (Exception e) {
      String path = null;
      try {
        path = node.getPath();
      } catch (RepositoryException ex) {
        LOG.error("Couldn't get path of the node");
      }
      LOG.error("Couldn't get size of the document: {}", path);
    }
    return size;
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
   */
  public EditorConfig createEditorConfig(String userSchema,
                                         String userHost,
                                         int userPort,
                                         String userId,
                                         String workspace,
                                         String fileId) throws RepositoryException {
    Node document = nodeByUUID(workspace, fileId);
    List<String> permissions = new ArrayList<>();

    if (document != null) {
      if (canEditDocument(document)) {
        permissions.add(USER_CAN_WRITE);
        permissions.add(USER_CAN_RENAME);
      } else {
        permissions.add(READ_ONLY);
      }
    }

    EditorConfig config = new EditorConfig(userId, fileId, permissions);
    String accessToken = idGenerator.generateStringID(config);
    config.setAccessToken(accessToken);
    configs.put(accessToken, config);
    return config;
  }

  /**
   * Stop.
   */
  @Override
  public void stop() {
    discoveryPlugin.stop();

  }

  /**
   * Node by UUID.
   *
   * @param uuid the uuid
   * @return the node
   * @throws RepositoryException the repository exception
   */
  protected Node nodeByUUID(String uuid) throws RepositoryException {
    String workspace = jcrService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
    return nodeByUUID(uuid, workspace);
  }

  /**
   * Node by UUID.
   *
   * @param uuid the uuid
   * @param workspace the workspace
   * @return the node
   * @throws RepositoryException the repository exception
   */
  protected Node nodeByUUID(String uuid, String workspace) throws RepositoryException {
    SessionProvider sp = sessionProviders.getSessionProvider(null);
    Session userSession = sp.getSession(workspace, jcrService.getCurrentRepository());
    return userSession.getNodeByUUID(uuid);
  }

  /**
   * Can edit document.
   *
   * @param node the node
   * @return true, if successful
   * @throws RepositoryException the repository exception
   */
  protected boolean canEditDocument(Node node) throws RepositoryException {
    boolean res = false;
    if (node != null) {
      String remoteUser = WCMCoreUtils.getRemoteUser();
      String superUser = WCMCoreUtils.getSuperUser();
      boolean locked = node.isLocked();
      if (locked && (remoteUser.equalsIgnoreCase(superUser) || node.getLock().getLockOwner().equals(remoteUser))) {
        locked = false;
      }
      res = !locked && PermissionUtil.canSetProperty(node);
    }
    if (!res && LOG.isDebugEnabled()) {
      LOG.debug("Cannot edit: {}", node != null ? node.getPath() : null);
    }
    return res;
  }

  /**
   * Gets the user.
   *
   * @param username the username
   * @return the user
   */
  protected User getUser(String username) {
    try {
      return organization.getUserHandler().findUserByName(username);
    } catch (Exception e) {
      LOG.error("Error searching user " + username, e);
      return null;
    }
  }

  /**
   * Sets the plugin.
   *
   * @param plugin the plugin
   */
  public void setPlugin(ComponentPlugin plugin) {
    Class<WOPIDiscoveryPlugin> pclass = WOPIDiscoveryPlugin.class;
    if (pclass.isAssignableFrom(plugin.getClass())) {
      discoveryPlugin = pclass.cast(plugin);
      LOG.info("Set WopiDiscoveryPlugin instance of " + plugin.getClass().getName());
    } else {
      throw new RuntimeException("WopiDiscoveryPlugin is not an instance of " + pclass.getName());
    }
  }

}