package org.exoplatform.officeonline;

import java.io.Serializable;
import java.net.URI;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.officeonline.exception.WopiDiscoveryNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.cache.CacheService;
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

/**
 * The Class WOPIService.
 */
public class WOPIService extends AbstractOfficeOnlineService {

  /** The Constant LOG. */
  protected static final Log    LOG                               = ExoLogger.getLogger(WOPIService.class);

  /** The Constant BASE_FILE_NAME. */
  protected static final String BASE_FILE_NAME                    = "BaseFileName";

  /** The Constant OWNER_ID. */
  protected static final String OWNER_ID                          = "OwnerId";

  /** The Constant SIZE. */
  protected static final String SIZE                              = "Size";

  /** The Constant USER_ID. */
  protected static final String USER_ID                           = "UserId";

  /** The Constant VERSION. */
  protected static final String VERSION                           = "Version";

  /** The Constant BREADCRUMB_BRAND_NAME. */
  protected static final String BREADCRUMB_BRAND_NAME             = "BreadcrumbBrandName";

  /** The Constant BREADCRUMB_BRAND_URL. */
  protected static final String BREADCRUMB_BRAND_URL              = "BreadcrumbBrandUrl";

  /** The Constant BREADCRUMB_FOLDER_NAME. */
  protected static final String BREADCRUMB_FOLDER_NAME            = "BreadcrumbFolderName";

  /** The Constant BREADCRUMB_FOLDER_URL. */
  protected static final String BREADCRUMB_FOLDER_URL             = "BreadcrumbFolderUrl";

  /** The Constant CLOSE_URL. */
  protected static final String CLOSE_URL                         = "CloseUrl";

  /** The Constant DOWNLOAD_URL. */
  protected static final String DOWNLOAD_URL                      = "DownloadUrl";

  /** The Constant FILE_VERSION_URL. */
  protected static final String FILE_VERSION_URL                  = "FileVersionUrl";

  /** The Constant HOST_EDIT_URL. */
  protected static final String HOST_EDIT_URL                     = "HostEditUrl";

  /** The Constant HOST_VIEW_URL. */
  protected static final String HOST_VIEW_URL                     = "HostViewUrl";

  /** The Constant SIGNOUT_URL. */
  protected static final String SIGNOUT_URL                       = "SignoutUrl";

  /** The Constant SUPPORTS_EXTENDED_LOCK_LENGTH. */
  protected static final String SUPPORTS_EXTENDED_LOCK_LENGTH     = "SupportsExtendedLockLength";

  /** The Constant SUPPORTS_GET_LOCK. */
  protected static final String SUPPORTS_GET_LOCK                 = "SupportsGetLock";

  /** The Constant SUPPORTS_LOCKS. */
  protected static final String SUPPORTS_LOCKS                    = "SupportsLocks";

  /** The Constant SUPPORTS_RENAME. */
  protected static final String SUPPORTS_RENAME                   = "SupportsRename";

  /** The Constant SUPPORTS_UPDATE. */
  protected static final String SUPPORTS_UPDATE                   = "SupportsUpdate";

  /** The Constant SUPPORTED_SHARE_URL_TYPES. */
  protected static final String SUPPORTED_SHARE_URL_TYPES         = "SupportedShareUrlTypes";

  /** The Constant IS_ANONYMOUS_USER. */
  protected static final String IS_ANONYMOUS_USER                 = "IsAnonymousUser";

  /** The Constant LICENSE_CHECK_FOR_EDIT_IS_ENABLED. */
  protected static final String LICENSE_CHECK_FOR_EDIT_IS_ENABLED = "LicenseCheckForEditIsEnabled";

  /** The Constant USER_FRIENDLY_NAME. */
  protected static final String USER_FRIENDLY_NAME                = "UserFriendlyName";

  /** The Constant SHARE_URL. */
  protected static final String SHARE_URL                         = "ShareUrl";

  /** The Constant SHARE_URL_READ_ONLY. */
  protected static final String SHARE_URL_READ_ONLY               = "ReadOnly";

  /** The Constant SHARE_URL_READ_WRITE. */
  protected static final String SHARE_URL_READ_WRITE              = "ReadWrite";

  /** The Constant TOKEN_CONFIGURATION_PROPERTIES. */
  protected static final String TOKEN_CONFIGURATION_PROPERTIES    = "token-configuration";

  /** The discovery plugin. */
  protected WOPIDiscoveryPlugin discoveryPlugin;

  /**
   * Instantiates a new WOPI service.
   *
   * @param sessionProviders the session providers
   * @param idGenerator the id generator
   * @param jcrService the jcr service
   * @param organization the organization
   * @param documentService the document service
   * @param authenticator the authenticator
   * @param identityRegistry the identity registry
   * @param cacheService the cache service
   * @param userACL the user ACL
   * @param initParams the init params
   */
  public WOPIService(SessionProviderService sessionProviders,
                     IDGeneratorService idGenerator,
                     RepositoryService jcrService,
                     OrganizationService organization,
                     DocumentService documentService,
                     Authenticator authenticator,
                     IdentityRegistry identityRegistry,
                     CacheService cacheService,
                     UserACL userACL,
                     InitParams initParams) {
    super(sessionProviders,
          idGenerator,
          jcrService,
          organization,
          documentService,
          authenticator,
          identityRegistry,
          cacheService,
          userACL);
    PropertiesParam param = initParams.getPropertiesParam(TOKEN_CONFIGURATION_PROPERTIES);
    String secretKey = param.getProperty(SECRET_KEY);
    if (secretKey != null && !secretKey.trim().isEmpty()) {
      byte[] decodedKey = Base64.getDecoder().decode(secretKey);
      SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
      activeCache.put(SECRET_KEY, key);
    } else {
      activeCache.put(SECRET_KEY, generateSecretKey());
    }

  }

  /**
   * Check file info.
   *
   * @param userSchema the user schema
   * @param userHost the user host
   * @param userPort the user port
   * @param accessToken the access token
   * @return the map
   * @throws RepositoryException the repository exception
   * @throws OfficeOnlineException the office online exception
   */
  public Map<String, Serializable> checkFileInfo(String userSchema,
                                                 String userHost,
                                                 int userPort,
                                                 String accessToken) throws RepositoryException, OfficeOnlineException {
    EditorConfig config = buildEditorConfig(accessToken);
    Map<String, Serializable> map = new HashMap<>();
    // remember real context state and session provider to restore them at the end
    ConversationState contextState = ConversationState.getCurrent();
    SessionProvider contextProvider = sessionProviders.getSessionProvider(null);
    try {
      if (!setUserConvoState(config.getUserId())) {
        LOG.error("Couldn't set user conversation state. UserId: {}", config.getUserId());
        throw new OfficeOnlineException("Cannot set conversation state " + config.getUserId());
      }
      Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
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

  /**
   * Start.
   */
  @Override
  public void start() {
    if (discoveryPlugin == null) {
      throw new WopiDiscoveryNotFoundException("WopiDiscoveryPlugin is not configured");
    }
    discoveryPlugin.start();

    LOG.info("WOPI Service started");

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
    map.put(USER_ID, ConversationState.getCurrent().getIdentity().getUserId());
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
    String user = ConversationState.getCurrent().getIdentity().getUserId();
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
    map.put(Permissions.READ_ONLY.toString(), !hasWritePermission);
    map.put(Permissions.USER_CAN_RENAME.toString(), hasWritePermission);
    map.put(Permissions.USER_CAN_WRITE.toString(), hasWritePermission);
    // TODO: Check permissions to parent folder
    map.put(Permissions.USER_CAN_NOT_WRITE_RELATIVE.toString(), !hasWritePermission);
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

    String downloadURL =
                       new StringBuilder(platformRestURL).append("/officeonline/editor/content/").append(accessToken).toString();
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
   * Generate secret key.
   *
   * @return the key
   */
  protected Key generateSecretKey() {
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
      keyGen.init(128);
      SecretKey key = keyGen.generateKey();
      return key;
    } catch (NoSuchAlgorithmException e) {
      LOG.error("Cannot generate secret key {}", e.getMessage());
      return null;
    }
  }
}
