package org.exoplatform.officeonline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.exoplatform.officeonline.Constants.READ_ONLY;
import static org.exoplatform.officeonline.Constants.USER_CAN_WRITE;
import static org.exoplatform.officeonline.Constants.USER_CAN_RENAME;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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
   */
  public OfficeOnlineEditorService(SessionProviderService sessionProviders,
                                   IDGeneratorService idGenerator,
                                   RepositoryService jcrService) {
    this.sessionProviders = sessionProviders;
    this.idGenerator = idGenerator;
    this.jcrService = jcrService;
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
   * @param workspace the workspace
   * @param uuid the UUID
   * @return the node
   * @throws RepositoryException the repository exception
   */
  protected Node nodeByUUID(String workspace, String uuid) throws RepositoryException {
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
  public boolean canEditDocument(Node node) throws RepositoryException {
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
