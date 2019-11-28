/*
 * 
 */
package org.exoplatform.officeonline;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.ecm.utils.text.Text;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.officeonline.exception.ActionNotFoundException;
import org.exoplatform.officeonline.exception.FileExtensionNotFoundException;
import org.exoplatform.officeonline.exception.FileLockedException;
import org.exoplatform.officeonline.exception.FileNotFoundException;
import org.exoplatform.officeonline.exception.IllegalFileNameException;
import org.exoplatform.officeonline.exception.InvalidFileNameException;
import org.exoplatform.officeonline.exception.LockMismatchException;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.officeonline.exception.PermissionDeniedException;
import org.exoplatform.officeonline.exception.SizeMismatchException;
import org.exoplatform.officeonline.exception.UpdateConflictException;
import org.exoplatform.officeonline.exception.WopiDiscoveryNotFoundException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.cms.documents.TrashService;
import org.exoplatform.services.cms.jcrext.activity.ActivityCommonService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * The Class WOPIService.
 */
public class WOPIService extends AbstractOfficeOnlineService {

  /** The Constant LOG. */
  protected static final Log         LOG                                 = ExoLogger.getLogger(WOPIService.class);

  /** The Constant BASE_FILE_NAME. */
  protected static final String      BASE_FILE_NAME                      = "BaseFileName";

  /** The Constant DEFAULT_FILENAME. */
  protected static final String      DEFAULT_FILENAME                    = "Untitled";

  /** The Constant OWNER_ID. */
  protected static final String      OWNER_ID                            = "OwnerId";

  /** The Constant FILES_ENDPOINT. */
  protected static final String      FILES_ENDPOINT                      = "/wopi/files/";

  /** The Constant SIZE. */
  protected static final String      SIZE                                = "Size";

  /** The Constant USER_ID. */
  protected static final String      USER_ID                             = "UserId";

  /** The Constant VERSION. */
  protected static final String      VERSION                             = "Version";

  /** The Constant BREADCRUMB_BRAND_NAME. */
  protected static final String      BREADCRUMB_BRAND_NAME               = "BreadcrumbBrandName";

  /** The Constant BREADCRUMB_BRAND_URL. */
  protected static final String      BREADCRUMB_BRAND_URL                = "BreadcrumbBrandUrl";

  /** The Constant BREADCRUMB_FOLDER_NAME. */
  protected static final String      BREADCRUMB_FOLDER_NAME              = "BreadcrumbFolderName";

  /** The Constant BREADCRUMB_FOLDER_URL. */
  protected static final String      BREADCRUMB_FOLDER_URL               = "BreadcrumbFolderUrl";

  /** The Constant CLOSE_URL. */
  protected static final String      CLOSE_URL                           = "CloseUrl";

  /** The Constant DOWNLOAD_URL. */
  protected static final String      DOWNLOAD_URL                        = "DownloadUrl";

  /** The Constant FILE_URL. */
  protected static final String      FILE_URL                            = "FileUrl";

  /** The Constant FILE_VERSION_URL. */
  protected static final String      FILE_VERSION_URL                    = "FileVersionUrl";

  /** The Constant HOST_EDIT_URL. */
  protected static final String      HOST_EDIT_URL                       = "HostEditUrl";

  /** The Constant HOST_VIEW_URL. */
  protected static final String      HOST_VIEW_URL                       = "HostViewUrl";

  /** The Constant SIGNOUT_URL. */
  protected static final String      SIGNOUT_URL                         = "SignoutUrl";

  /** The Constant SUPPORTS_EXTENDED_LOCK_LENGTH. */
  protected static final String      SUPPORTS_EXTENDED_LOCK_LENGTH       = "SupportsExtendedLockLength";

  /** The Constant SUPPORTS_GET_LOCK. */
  protected static final String      SUPPORTS_GET_LOCK                   = "SupportsGetLock";

  /** The Constant SUPPORTS_LOCKS. */
  protected static final String      SUPPORTS_LOCKS                      = "SupportsLocks";

  /** The Constant SUPPORTS_RENAME. */
  protected static final String      SUPPORTS_RENAME                     = "SupportsRename";

  /** The Constant SUPPORTS_UPDATE. */
  protected static final String      SUPPORTS_UPDATE                     = "SupportsUpdate";

  /** The Constant SUPPORTS_DELETE_FILE. */
  protected static final String      SUPPORTS_DELETE_FILE                = "SupportsDeleteFile";

  /** The Constant SUPPORTS_USER_INFO. */
  protected static final String      SUPPORTS_USER_INFO                  = "SupportsUserInfo";

  /** The Constant SUPPORTED_SHARE_URL_TYPES. */
  protected static final String      SUPPORTED_SHARE_URL_TYPES           = "SupportedShareUrlTypes";

  /** The Constant IS_ANONYMOUS_USER. */
  protected static final String      IS_ANONYMOUS_USER                   = "IsAnonymousUser";

  /** The Constant USER_INFO. */
  protected static final String      USER_INFO                           = "UserInfo";

  /** The Constant LICENSE_CHECK_FOR_EDIT_IS_ENABLED. */
  protected static final String      LICENSE_CHECK_FOR_EDIT_IS_ENABLED   = "LicenseCheckForEditIsEnabled";

  /** The Constant USER_FRIENDLY_NAME. */
  protected static final String      USER_FRIENDLY_NAME                  = "UserFriendlyName";

  /** The Constant PLACEHOLDER_WOPISRC. */
  protected static final String      PLACEHOLDER_WOPISRC                 = "&wopisrc=";

  /** The Constant PLACEHOLDER_DC_LLCC. */
  protected static final String      PLACEHOLDER_DC_LLCC                 = "&DC_LLCC=";

  /** The Constant PLACEHOLDER_UI_LLCC. */
  protected static final String      PLACEHOLDER_UI_LLCC                 = "&UI_LLCC=";

  /** The Constant SHARE_URL. */
  protected static final String      SHARE_URL                           = "ShareUrl";

  /** The Constant SHARE_URL_READ_ONLY. */
  protected static final String      SHARE_URL_READ_ONLY                 = "ReadOnly";

  /** The Constant SHARE_URL_READ_WRITE. */
  protected static final String      SHARE_URL_READ_WRITE                = "ReadWrite";

  /** The Constant TOKEN_CONFIGURATION_PROPERTIES. */
  protected static final String      TOKEN_CONFIGURATION_PROPERTIES      = "token-configuration";

  /** The Constant BREADCRUMB_CONFIGURATION_PROPERTIES. */
  protected static final String      BREADCRUMB_CONFIGURATION_PROPERTIES = "breadcrumb-configuration";

  /** The Constant WOPI_CONFIGURATION_PROPERTIES. */
  protected static final String      WOPI_CONFIGURATION_PROPERTIES       = "wopi-configuration";

  /** The Constant WOPI_URL. */
  protected static final String      WOPI_URL                            = "wopi-url";

  /** The Constant BRAND_NAME. */
  protected static final String      BRAND_NAME                          = "brand-name";

  /** The Constant WOPITEST_EXTENSION. */
  protected static final String      WOPITEST_EXTENSION                  = "wopitest";

  /** The Constant WOPITEST_ACTION. */
  protected static final String      WOPITEST_ACTION                     = "view";

  /** The Constant MAX_FILENAME_LENGHT. */
  protected static final int         MAX_FILENAME_LENGHT                 = 510;

  /** The Constant USERIONFO_CACHE_NAME. */
  public static final String         USERINFO_CACHE_NAME                 = "officeonline.userinfo.Cache".intern();

  /** The trash service. */
  protected final TrashService       trashService;

  /** The discovery plugin. */
  protected WOPIDiscoveryPlugin      discoveryPlugin;

  /** The lock manager. */
  protected WOPILockManagerPlugin    lockManager;

  /** The file extensions. */
  protected Map<String, String>      fileExtensions                      = new HashMap<>();

  /** The brand name. */
  protected String                   brandName;

  /** The wopi files url. */
  protected String                   wopiUrl;

  /** The platform scheme. */
  protected String                   platformScheme;

  /** The platform host. */
  protected String                   platformHost;

  /** The platform port. */
  protected int                      platformPort;

  /** The user info cache. */
  protected ExoCache<String, String> userInfoCache;

  /**
   * Instantiates a new WOPI service.
   *
   * @param sessionProviders the session providers
   * @param jcrService the jcr service
   * @param organization the organization
   * @param documentService the document service
   * @param cacheService the cache service
   * @param userACL the user ACL
   * @param trashService the trash service
   * @param initParams the init params
   */
  public WOPIService(SessionProviderService sessionProviders,
                     RepositoryService jcrService,
                     OrganizationService organization,
                     DocumentService documentService,
                     CacheService cacheService,
                     UserACL userACL,
                     TrashService trashService,
                     InitParams initParams) {
    super(sessionProviders, jcrService, organization, documentService, cacheService, userACL);
    this.trashService = trashService;
    PropertiesParam tokenParam = initParams.getPropertiesParam(TOKEN_CONFIGURATION_PROPERTIES);
    String secretKey = tokenParam.getProperty(SECRET_KEY);
    if (secretKey != null && !secretKey.trim().isEmpty()) {
      byte[] decodedKey = Base64.getDecoder().decode(secretKey);
      SecretKey key = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
      keyCache.put(SECRET_KEY, key);
    } else {
      keyCache.put(SECRET_KEY, generateSecretKey());
    }

    this.userInfoCache = cacheService.getCacheInstance(USERINFO_CACHE_NAME);
    PropertiesParam breadcrumbParam = initParams.getPropertiesParam(BREADCRUMB_CONFIGURATION_PROPERTIES);
    brandName = breadcrumbParam.getProperty(BRAND_NAME);

    PropertiesParam wopiFilesUrlParam = initParams.getPropertiesParam(WOPI_CONFIGURATION_PROPERTIES);
    wopiUrl = wopiFilesUrlParam.getProperty(WOPI_URL);

    initFileExtensions();
  }

  /**
   * Inits the file extensions.
   */
  protected void initFileExtensions() {
    fileExtensions.put("application/msword", "doc");
    fileExtensions.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
    fileExtensions.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx");
    fileExtensions.put("application/vnd.ms-word.document.macroEnabled.1", "docm");
    fileExtensions.put("application/vnd.ms-word.template.macroEnabled.12", "dotm");

    fileExtensions.put("application/vnd.ms-excel", "xls");
    fileExtensions.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
    fileExtensions.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx");
    fileExtensions.put("application/vnd.ms-excel.sheet.macroEnabled.12", "xlsm");
    fileExtensions.put("application/vnd.ms-excel.template.macroEnabled.12", "xltm");
    fileExtensions.put("application/vnd.ms-excel.addin.macroEnabled.12", "xlam");
    fileExtensions.put("application/vnd.ms-excel.sheet.binary.macroEnabled.12", "xlsb");

    fileExtensions.put("application/vnd.ms-powerpoint", "ppt");
    fileExtensions.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
    fileExtensions.put("application/vnd.openxmlformats-officedocument.presentationml.template", "potx");
    fileExtensions.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx");
    fileExtensions.put("application/vnd.ms-powerpoint.addin.macroEnabled.12", "ppam");
    fileExtensions.put("application/vnd.ms-powerpoint.presentation.macroEnabled.12", "pptm");
    fileExtensions.put("application/vnd.ms-powerpoint.template.macroEnabled.12", "potm");
    fileExtensions.put("application/vnd.ms-powerpoint.slideshow.macroEnabled.12", "ppsm");

  }

  /**
   * Put file.
   *
   * @param config the config
   * @param lockId the lock id
   * @param data the data
   * @throws Exception the exception
   */
  public void putFile(EditorConfig config, String lockId, InputStream data) throws Exception {
    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    try {
      if (PermissionUtil.canSetProperty(node) && config.permissions.contains(Permissions.USER_CAN_WRITE)) {
        Node content = node.getNode(JCR_CONTENT);
        if (!node.isLocked()) {
          long size = content.getProperty(JCR_DATA).getLength();
          if (size != 0) {
            throw new SizeMismatchException("File is unlocked and size isn't equal to 0.", "");
          }
        } else {
          checkNodeLock(node, lockId);
        }
        content.setProperty(JCR_DATA, data);
        Calendar editedTime = Calendar.getInstance();
        content.setProperty(JCR_LAST_MODIFIED, editedTime);
        if (content.hasProperty(EXO_DATE_MODIFIED)) {
          content.setProperty(EXO_DATE_MODIFIED, editedTime);
        }
        if (content.hasProperty(EXO_LAST_MODIFIED_DATE)) {
          content.setProperty(EXO_LAST_MODIFIED_DATE, editedTime);
        }
        if (node.hasProperty(EXO_LAST_MODIFIED_DATE)) {
          node.setProperty(EXO_LAST_MODIFIED_DATE, editedTime);
        }
        if (node.hasProperty(EXO_DATE_MODIFIED)) {
          node.setProperty(EXO_DATE_MODIFIED, editedTime);
        }
        if (node.hasProperty(EXO_LAST_MODIFIER)) {
          node.setProperty(EXO_LAST_MODIFIER, config.getUserId());
        }

        node.save();

        if (checkout(node)) {
          // Make a new version from the downloaded state
          node.checkin();
          // Since 1.2.0-RC01 we check-out the document to let (more) other
          // actions in ECMS appear on it
          node.checkout();
        }

        if (data != null) {
          try {
            data.close();
          } catch (IOException e) {
            LOG.error("Error closing data stream. FileID:" + config.getFileId());
          }
        }
      } else {
        throw new PermissionDeniedException("Cannnot update file. Permission denied");
      }
    } catch (RepositoryException e) {
      LOG.error("Cannot save document content.", e);
      throw new OfficeOnlineException("Cannot perform putFile operation. FileId: " + config.getFileId() + ", workspace: "
          + config.getWorkspace());
    }

  }

  /**
   * Checkout.
   *
   * @param node the node
   * @return true, if successful
   * @throws RepositoryException the repository exception
   */
  protected boolean checkout(Node node) throws RepositoryException {
    if (node.isNodeType("mix:versionable")) {
      if (!node.isCheckedOut()) {
        node.checkout();
      }
      return true;
    }
    return false;
  }

  /**
   * Check file info.
   *
   * @param config the config
   * @return the map
   * @throws RepositoryException the repository exception
   * @throws FileNotFoundException the file not found exception
   */
  public Map<String, Serializable> checkFileInfo(EditorConfig config) throws RepositoryException,
                                                                                          FileNotFoundException {

    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    Map<String, Serializable> map = new HashMap<>();
    addRequiredProperties(map, node);
    addHostCapabilitiesProperties(map);
    addUserMetadataProperties(map);
    addUserPermissionsProperties(map, node);
    addFileURLProperties(map, node, config.getAccessToken(), config.getPlatformUrl());
    addBreadcrumbProperties(map, node, config.getPlatformUrl());
    return map;
  }

  /**
   * Verify proof key. Runs wopi discovery in case of failure/success with old key
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
    boolean successWithOldKey = false;
    if (!res && StringUtils.isNotBlank(oldProofKeyHeader)) {
      res = ProofKeyHelper.verifyProofKey(discoveryPlugin.getProofKey(), oldProofKeyHeader, expectedProofBytes);
      if (!res) {
        res = ProofKeyHelper.verifyProofKey(discoveryPlugin.getOldProofKey(), proofKeyHeader, expectedProofBytes);
        successWithOldKey = res;
      }
    }

    // Rerun discovery in case of success with old key or failure
    if (!res || successWithOldKey) {
      // TODO: should we run in in separate thread?
      discoveryPlugin.loadDiscovery();
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
   * Sets the plugin.
   *
   * @param plugin the plugin
   */
  public void setWOPILockManagerPlugin(ComponentPlugin plugin) {
    Class<WOPILockManagerPlugin> pclass = WOPILockManagerPlugin.class;
    if (pclass.isAssignableFrom(plugin.getClass())) {
      lockManager = pclass.cast(plugin);
      LOG.info("Set WOPILockManagerPlugin instance of " + plugin.getClass().getName());
    } else {
      throw new WopiDiscoveryNotFoundException("WOPILockManagerPlugin is not an instance of " + pclass.getName());
    }
  }

  /**
   * Gets the action url.
   *
   * @param requestInfo the request info
   * @param fileId the file id
   * @param workspace the workspace
   * @param action the action
   * @return the action url
   * @throws RepositoryException the repository exception
   * @throws OfficeOnlineException the office online exception
   */
  public String getActionUrl(RequestInfo requestInfo, String fileId, String workspace, String action) throws RepositoryException,
                                                                                                      OfficeOnlineException {
    Node node = nodeByUUID(fileId, workspace);
    String extension = getFileExtension(node);
    if (extension.equals(WOPITEST_EXTENSION)) {
      action = WOPITEST_ACTION;
    }
    String actionURL = discoveryPlugin.getActionUrl(extension, action);
    if (actionURL != null) {
      return new StringBuilder(actionURL).append(PLACEHOLDER_WOPISRC)
                                         .append(getWOPISrc(requestInfo, fileId))
                                         .append(PLACEHOLDER_DC_LLCC)
                                         .append(requestInfo.getLocale().toString())
                                         .append(PLACEHOLDER_UI_LLCC)
                                         .append(requestInfo.getLocale().toString())
                                         .toString();
    } else {
      throw new ActionNotFoundException("Cannot find actionURL for file extension " + extension + " and action: " + action);
    }
  }

  /**
   * Rename file.
   *
   * @param config the config
   * @param newTitle the new title
   * @param lockId the lock id
   * @return the string
   * @throws RepositoryException the repository exception
   * @throws OfficeOnlineException the office online exception
   * @throws LockMismatchException the lock mismatch exception
   */
  public String renameFile(EditorConfig config, String newTitle, String lockId) throws RepositoryException,
                                                                                OfficeOnlineException,
                                                                                LockMismatchException {

    newTitle = Text.escapeIllegalJcrChars(newTitle);
    // Check and escape newTitle
    if (StringUtils.isBlank(newTitle)) {
      throw new InvalidFileNameException("Requested title is not allowed due to using illegal JCR chars");
    }

    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    if (trashService.isInTrash(node)) {
      throw new FileNotFoundException("File has been deleted. FileId: " + config.getFileId() + ", workspace: "
          + config.getWorkspace());
    }

    if (!canUpdate(node) || !config.getPermissions().contains(Permissions.USER_CAN_RENAME)) {
      throw new PermissionDeniedException("Cannot rename document. Permission denied");
    }
    checkNodeLock(node, lockId);

    Node parentNode = node.getParent();
    if (parentNode.canAddMixin(NodetypeConstant.MIX_REFERENCEABLE)) {
      parentNode.addMixin(NodetypeConstant.MIX_REFERENCEABLE);
      parentNode.save();
    }

    /*
      We cannot rename file uploaded from Activity Stream
      PLF issue: https://jira.exoplatform.org/browse/PLF-8596
    if (!node.hasPermission(PermissionType.REMOVE)) {
      Session systemSession = jcrService.getCurrentRepository().getSystemSession(config.getWorkspace());
      NodeImpl systemNode = (NodeImpl) systemSession.getNodeByUUID(config.getFileId());
         
      systemNode.addMixin(EXO_PRIVILEGEABLE);
      systemNode.setPermission(config.getUserId(),
                               new String[] { PermissionType.REMOVE, PermissionType.READ, PermissionType.ADD_NODE,
                                   PermissionType.SET_PROPERTY });
      systemNode.save();
    } */

    // Get current file extension
    String extension = null;
    try {
      extension = getFileExtension(node);
    } catch (FileExtensionNotFoundException e) {
      LOG.warn("Cannot get file extension.", e);
    }

    String name = newTitle;
    if (extension != null) {
      name = new StringBuilder(newTitle).append(".").append(extension).toString();
    }

    parentNode.getSession().move(node.getPath(), parentNode.getPath() + "/" + name);
    node.setProperty(EXO_LAST_MODIFIER, config.getUserId());
    node.setProperty(EXO_NAME, name);
    node.setProperty(EXO_TITLE, name);
    node.refresh(true);
    parentNode.getSession().save();
    // Return title without file extension
    return newTitle;
  }

  /**
   * Checks parent node permissions.
   *
   * @param node the node
   * @return true if user can rename
   * @throws RepositoryException the repository exception
   */
  protected boolean canUpdate(Node node) throws RepositoryException {
    NodeImpl parent;
    try {
      parent = (NodeImpl) node.getParent();
    } catch (AccessDeniedException e) {
      LOG.warn("Cannot get access to the parent node ", e);
      return false;
    }
    return parent.hasPermission(PermissionType.READ) && parent.hasPermission(PermissionType.ADD_NODE)
        && parent.hasPermission(PermissionType.SET_PROPERTY);
  }

  /**
   * Gets the file extension.
   *
   * @param node the node
   * @return the file extension
   * @throws RepositoryException the repository exception
   * @throws FileExtensionNotFoundException the file extension not found exception
   */
  protected String getFileExtension(Node node) throws RepositoryException, FileExtensionNotFoundException {
    String title = node.getProperty(Utils.EXO_TITLE).getString();
    if (title.contains(".")) {
      return title.substring(title.lastIndexOf(".") + 1);
    }

    String mimeType;
    if (node.isNodeType(Utils.NT_FILE)) {
      mimeType = node.getNode(Utils.JCR_CONTENT).getProperty(Utils.JCR_MIMETYPE).getString();
    } else {
      mimeType = new MimeTypeResolver().getMimeType(node.getName());
    }

    String extension = fileExtensions.get(mimeType);
    if (extension != null) {
      return extension;
    } else {
      throw new FileExtensionNotFoundException("Cannot get file extension. FileId: " + node.getUUID() + ". Title: " + title);
    }
  }

  /**
   * Gets the WOPI src.
   *
   * @param requestInfo the request info
   * @param fileId the file id
   * @return the WOPI src
   */
  public String getWOPISrc(RequestInfo requestInfo, String fileId) {
    return new StringBuilder(wopiUrl).append("/files/").append(fileId).toString();
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
    if (LOG.isDebugEnabled()) {
      LOG.debug("Excel edit URL: " + excelEdit);
      LOG.debug("Excel view URL: " + excelView);
      LOG.debug("Word edit URL: " + wordEdit);
      LOG.debug("Excel view URL: " + wordView);
      LOG.debug("PowerPoint edit URL: " + powerPointEdit);
      LOG.debug("PowerPoint view URL: " + powerPointView);
    }
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
   * @throws RepositoryException the repository exception
   */
  protected void addRequiredProperties(Map<String, Serializable> map, Node node) throws RepositoryException {
    map.put(BASE_FILE_NAME, node.getProperty(EXO_TITLE).getString());
    map.put(OWNER_ID, node.getProperty(EXO_OWNER).getString());
    map.put(SIZE, getSize(node));
    map.put(USER_ID, ConversationState.getCurrent().getIdentity().getUserId());
    if (node.isNodeType(MIX_VERSIONABLE)) {
      map.put(VERSION, node.getBaseVersion().getName());
    }
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
    map.put(SUPPORTS_DELETE_FILE, true);
    map.put(SUPPORTS_USER_INFO, true);
    // TODO: Introduce SHARE_URL_TYPES
    // map.put(SUPPORTED_SHARE_URL_TYPES, (Serializable) Arrays.asList(SHARE_URL_READ_ONLY, SHARE_URL_READ_WRITE));
  }

  /**
   * Adds the user metadata properties.
   *
   * @param map the map
   */
  protected void addUserMetadataProperties(Map<String, Serializable> map) {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    User user = getUser(userId);
    String displayName = user != null ? user.getDisplayName() : userId;
    map.put(IS_ANONYMOUS_USER, false);
    map.put(LICENSE_CHECK_FOR_EDIT_IS_ENABLED, true);
    map.put(USER_FRIENDLY_NAME, displayName);
    String userInfo = userInfoCache.get(userId);
    if (userInfo != null) {
      map.put(USER_INFO, userInfo);
    }
  }

  /**
   * Adds the user permissions properties.
   *
   * @param map the map
   * @param node the node
   * @throws RepositoryException the repository exception
   */
  protected void addUserPermissionsProperties(Map<String, Serializable> map, Node node) throws RepositoryException {
    boolean canEdit = PermissionUtil.canSetProperty(node);
    boolean canUpdate = canUpdate(node);
    map.put(Permissions.READ_ONLY.toString(), !canEdit);
    map.put(Permissions.USER_CAN_RENAME.toString(), canUpdate);
    map.put(Permissions.USER_CAN_WRITE.toString(), canEdit);
    map.put(Permissions.USER_CAN_NOT_WRITE_RELATIVE.toString(), !canUpdate);
  }

  /**
   * Adds the file URL properties.
   *
   * @param map the map
   * @param node the node
   * @param accessToken the access token
   * @param platformUrl the platform url
   * @throws RepositoryException the repository exception
   */
  protected void addFileURLProperties(Map<String, Serializable> map,
                                      Node node,
                                      AccessToken accessToken,
                                      String platformUrl) throws RepositoryException {
    String explorerLink = explorerLink(node.getPath());
    URI explorerUri = explorerUri(platformUrl, explorerLink);
    if (explorerUri != null) {
      map.put(CLOSE_URL, explorerUri.toString());
      map.put(FILE_VERSION_URL, explorerUri.toString());
    }
    String platformRestURL = new StringBuilder(platformUrl).append('/')
                                                           .append(PortalContainer.getCurrentRestContextName())
                                                           .toString();

    String downloadURL = new StringBuilder(platformRestURL).append("/officeonline/editor/content/")
                                                           .append(node.getUUID())
                                                           .append("?access_token=")
                                                           .append(accessToken.getToken())
                                                           .toString();
    map.put(DOWNLOAD_URL, downloadURL);
    map.put(HOST_EDIT_URL, getEditorURL(node.getUUID(), platformUrl));
    // TODO: change to view action
    map.put(HOST_VIEW_URL, getEditorURL(node.getUUID(), platformUrl));
    map.put(FILE_URL, downloadURL);

  }

  /**
   * Adds the breadcrumb properties.
   *
   * @param map the map
   * @param node the node
   * @param platformUrl the platform url
   */
  protected void addBreadcrumbProperties(Map<String, Serializable> map, Node node, String platformUrl) {
    // TODO: replace by real values
    map.put(BREADCRUMB_BRAND_NAME, brandName);
    map.put(BREADCRUMB_BRAND_URL, platformUrl);
    try {
      Node parent = node.getParent();
      String url = explorerUri(platformUrl, explorerLink(parent.getPath())).toString();
      if (parent.hasProperty(EXO_TITLE)) {
        map.put(BREADCRUMB_FOLDER_NAME, parent.getProperty(EXO_TITLE).getString());
      } else if (parent.hasProperty(EXO_NAME)) {
        map.put(BREADCRUMB_FOLDER_NAME, parent.getProperty(EXO_NAME).getString());
      }
      map.put(BREADCRUMB_FOLDER_URL, url);
    } catch (RepositoryException e) {
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

  /**
   * Lock.
   *
   * @param config the config
   * @param lockId the lock id
   * @throws LockMismatchException the lock mismatch exception
   * @throws RepositoryException the repository exception
   * @throws FileNotFoundException the file not found exception
   */
  public void lock(EditorConfig config, String lockId) throws LockMismatchException, RepositoryException, FileNotFoundException {
    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    lockManager.lock(node, lockId);
  }

  /**
   * Gets the lock.
   *
   * @param config the config
   * @return the lock
   * @throws FileNotFoundException the file not found exception
   * @throws RepositoryException the repository exception
   */
  public String getLockId(EditorConfig config) throws FileNotFoundException, RepositoryException {

    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    FileLock lock = lockManager.getLock(node);
    return lock != null ? lock.getLockId() : null;
  }

  /**
   * Relock.
   *
   * @param config the config
   * @param providedLock the provided lock
   * @param oldLock the old lock
   * @throws LockMismatchException the lock mismatch exception
   * @throws RepositoryException the repository exception
   * @throws FileNotFoundException the file not found exception
   */
  public void relock(EditorConfig config, String providedLock, String oldLock) throws LockMismatchException,
                                                                               RepositoryException,
                                                                               FileNotFoundException {

    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    lockManager.unlock(node, oldLock, config.getWorkspace());
    lockManager.lock(node, providedLock);
  }

  /**
   * Unlock.
   *
   * @param config the config
   * @param providedLock the provided lock
   * @throws LockMismatchException the lock mismatch exception
   * @throws RepositoryException the repository exception
   * @throws FileNotFoundException the file not found exception
   */
  public void unlock(EditorConfig config,
                     String providedLock) throws LockMismatchException, RepositoryException, FileNotFoundException {

    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    lockManager.unlock(node, providedLock, config.getWorkspace());
  }

  /**
   * Refresh lock.
   *
   * @param config the config
   * @param lockId the lock id
   * @throws LockMismatchException the lock mismatch exception
   * @throws RepositoryException the repository exception
   * @throws FileNotFoundException the file not found exception
   */
  public void refreshLock(EditorConfig config,
                          String lockId) throws LockMismatchException, RepositoryException, FileNotFoundException {

    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    lockManager.refreshLock(node, lockId);
  }

  /**
   * Put relative file in suggested mode.
   *
   * @param config the config
   * @param target the target
   * @param data the data
   * @return the fileId
   * @throws RepositoryException the repository exception
   * @throws FileNotFoundException the file not found exception
   * @throws FileExtensionNotFoundException the file extension not found exception
   * @throws PermissionDeniedException the permission denied exception
   */
  public String putSuggestedFile(EditorConfig config, String target, InputStream data) throws RepositoryException,
                                                                                       FileNotFoundException,
                                                                                       FileExtensionNotFoundException,
                                                                                       PermissionDeniedException {
    if (config.getPermissions().contains(Permissions.USER_CAN_NOT_WRITE_RELATIVE)) {
      throw new PermissionDeniedException("User " + config.getUserId() + " cannot write relative");
    }
    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    String oldName = node.getName();
    String filename = target;
    // Target is an extension
    if (target.startsWith(".")) {
      String name = oldName.substring(0, oldName.lastIndexOf("."));
      filename = name + target;
    }

    filename = Text.escapeIllegalJcrChars(filename);
    // Check and escape newTitle
    if (StringUtils.isBlank(filename)) {
      filename = DEFAULT_FILENAME + oldName.substring(oldName.lastIndexOf("."));
    }

    // TODO: handle long filenames
    if (filename.length() > MAX_FILENAME_LENGHT) {
      String extension = filename.substring(filename.lastIndexOf("."));
      int length = filename.length() - MAX_FILENAME_LENGHT + extension.length();
      filename = filename.substring(0, length) + extension;
    }

    Node parent = node.getParent();
    return createFile(parent, filename, data);
  }

  /**
   * Put relative file.
   *
   * @param config the config
   * @param filename the filename
   * @param overwrite the overwrite
   * @param data the data
   * @return the node uuid
   * @throws IllegalFileNameException the illegal file name exception
   * @throws FileNotFoundException the file not found exception
   * @throws RepositoryException the repository exception
   * @throws FileLockedException the file locked exception
   * @throws UpdateConflictException the update conflict exception
   * @throws FileExtensionNotFoundException the file extension not found exception
   * @throws PermissionDeniedException the permission denied exception
   */
  public String putRelativeFile(EditorConfig config,
                                String filename,
                                boolean overwrite,
                                InputStream data) throws IllegalFileNameException,
                                                  FileNotFoundException,
                                                  RepositoryException,
                                                  FileLockedException,
                                                  UpdateConflictException,
                                                  FileExtensionNotFoundException,
                                                  PermissionDeniedException {

    if (config.getPermissions().contains(Permissions.USER_CAN_NOT_WRITE_RELATIVE)) {
      throw new PermissionDeniedException("User " + config.getUserId() + " cannot write relative");
    }

    filename = Text.escapeIllegalJcrChars(filename);

    // TODO: handle long filenames by trimming it. Remember about the uniqueness.
    // A trimmed node name should not conflict with another node name starting with the same prefix
    // but being truncated before an unique part.
    // The max length for node name is 512 chars (eXo JCR limit)
    if (StringUtils.isBlank(filename) || filename.length() > MAX_FILENAME_LENGHT) {
      throw new IllegalFileNameException("Provided filename is illegal", filename);
    }

    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    Node parent = node.getParent();

    String path = parent.getPath() + "/" + filename;
    Session userSession = node.getSession();
    if (userSession.itemExists(path)) {
      if (!overwrite) {
        throw new UpdateConflictException("Overwrite is not allowed", null, filename);
      }

      Node targetNode = (Node) userSession.getItem(path);
      if (targetNode.isLocked()) {
        FileLock lock = lockManager.getLock(targetNode);
        throw new FileLockedException("File is locked", lock != null ? lock.getLockId() : null, filename);
      }

      long editedTime = System.currentTimeMillis();
      Node content = targetNode.getNode(JCR_CONTENT);

      content.setProperty("jcr:lastModified", editedTime);
      if (content.hasProperty("exo:dateModified")) {
        content.setProperty("exo:dateModified", editedTime);
      }
      if (content.hasProperty("exo:lastModifiedDate")) {
        content.setProperty("exo:lastModifiedDate", editedTime);
      }
      if (targetNode.hasProperty("exo:lastModifiedDate")) {
        targetNode.setProperty("exo:lastModifiedDate", editedTime);
      }

      if (targetNode.hasProperty("exo:dateModified")) {
        targetNode.setProperty("exo:dateModified", editedTime);
      }
      if (targetNode.hasProperty("exo:lastModifier")) {
        targetNode.setProperty("exo:lastModifier", config.getUserId());
      }
      content.setProperty("jcr:data", data);
      parent.save();
      try {
        data.close();
      } catch (IOException e) {
        LOG.error("Cannot close data input stream", e);
      }
      return targetNode.getUUID();
    } else {
      return createFile(parent, filename, data);
    }
  }

  /**
   * Creates the file.
   *
   * @param parent the parent
   * @param filename the filename
   * @param data the data
   * @return the string
   * @throws RepositoryException the repository exception
   * @throws FileExtensionNotFoundException the file extension not found exception
   */
  protected String createFile(Node parent, String filename, InputStream data) throws RepositoryException,
                                                                              FileExtensionNotFoundException {
    // Add node
    Node addedNode = parent.addNode(filename, Utils.NT_FILE);

    // Set title
    if (!addedNode.hasProperty(Utils.EXO_TITLE)) {
      addedNode.addMixin(Utils.EXO_RSS_ENABLE);
    }
    // Enable versioning
    if (addedNode.canAddMixin(MIX_VERSIONABLE)) {
      addedNode.addMixin(MIX_VERSIONABLE);
    }
    if (addedNode.hasProperty("exo:name")) {
      addedNode.setProperty("exo:name", filename);
    }
    addedNode.setProperty(Utils.EXO_TITLE, filename);

    Node content = addedNode.addNode("jcr:content", "nt:resource");

    content.setProperty("jcr:data", data);
    String extension = filename.substring(filename.lastIndexOf(".") + 1);
    content.setProperty("jcr:mimeType", getMimeTypeByExtension(extension));
    content.setProperty("jcr:lastModified", new GregorianCalendar());
    ListenerService listenerService = WCMCoreUtils.getService(ListenerService.class);
    try {
      listenerService.broadcast(ActivityCommonService.FILE_CREATED_ACTIVITY, null, addedNode);
    } catch (Exception e) {
      LOG.error("Cannot broadcast FILE_CREATED_ACTIVITY event", e);
    }
    parent.save();
    try {
      data.close();
    } catch (IOException e) {
      LOG.error("Cannot close data input stream", e);
    }
    return addedNode.getUUID();
  }

  /**
   * Gets the mime type by extension.
   *
   * @param extension the extension
   * @return the mime type by extension
   * @throws FileExtensionNotFoundException the file extension not found exception
   */
  public String getMimeTypeByExtension(String extension) throws FileExtensionNotFoundException {
    for (Entry<String, String> entry : fileExtensions.entrySet()) {
      if (entry.getValue().equals(extension)) {
        return entry.getKey();
      }
    }
    if (extension.equals("wopitest") || extension.equals("wopitestx")) {
      return "application/octet-stream";
    }
    throw new FileExtensionNotFoundException("Cannot find file extension " + extension);
  }

  /**
   * Gets the file version.
   *
   * @param fileId the file id
   * @param workspace the workspace
   * @return the file version
   * @throws FileNotFoundException the file not found exception
   * @throws RepositoryException the repository exception
   */
  public String getFileVersion(String fileId, String workspace) throws FileNotFoundException, RepositoryException {
    Node node = nodeByUUID(fileId, workspace);
    if (node.isNodeType(MIX_VERSIONABLE)) {
      return node.getBaseVersion().getName();
    }
    return null;
  }

  /**
   * Gets the file name.
   *
   * @param fileId the file id
   * @param workspace the workspace
   * @return the file name
   * @throws FileNotFoundException the file not found exception
   * @throws RepositoryException the repository exception
   */
  public String getFileName(String fileId, String workspace) throws FileNotFoundException, RepositoryException {
    Node node = nodeByUUID(fileId, workspace);
    return node.getName();
  }

  /**
   * Delete.
   *
   * @param config the config
   * @param lockId the lock id
   * @throws RepositoryException the repository exception
   * @throws OfficeOnlineException the office online exception
   * @throws LockMismatchException the lock mismatch exception
   */
  public void delete(EditorConfig config,
                     String lockId) throws RepositoryException, OfficeOnlineException, LockMismatchException {
    Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
    checkNodeLock(node, lockId);
    try {
      if (!trashService.isInTrash(node)) {
        trashService.moveToTrash(node, sessionProviders.getSessionProvider(null));
      }
    } catch (Exception e) {
      LOG.error("Cannot move node to trash.", e);
      throw new OfficeOnlineException("Failed to delete file with fileId: " + config.getFileId());
    }
  }

  /**
   * Check node lock.
   *
   * @param node the node
   * @param lockId the lock id
   * @throws LockMismatchException the lock mismatch exception
   * @throws RepositoryException the repository exception
   */
  protected void checkNodeLock(Node node, String lockId) throws LockMismatchException, RepositoryException {
    if (node.isLocked()) {
      FileLock lock = lockManager.getLock(node);
      // TODO: handle case when lock is null
      if (!lockId.equals(lock.getLockId())) {
        throw new LockMismatchException("Given lock is different from the file lock", lock.getLockId());
      }
      node.getSession().addLockToken(lock.getLockToken());
    }
  }

  /**
   * Put user info.
   *
   * @param userId the user id
   * @param userInfo the user info
   */
  public void putUserInfo(String userId, String userInfo) {
    userInfoCache.put(userId, userInfo);
  }

}
