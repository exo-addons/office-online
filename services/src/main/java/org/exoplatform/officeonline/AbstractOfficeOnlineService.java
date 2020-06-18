package org.exoplatform.officeonline;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.picocontainer.Startable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.utils.permission.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.officeonline.exception.FileNotFoundException;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.wcm.core.NodetypeConstant;

/**
 * The Class AbstractOfficeOnlineService.
 */
public abstract class AbstractOfficeOnlineService implements Startable {

  /** The Constant LOG. */
  protected static final Log             LOG                       = ExoLogger.getLogger(AbstractOfficeOnlineService.class);

  /** The Constant UTF_8. */
  protected static final String          UTF_8                     = "utf-8";

  /** The Constant KEY_CACHE_NAME. */
  public static final String             KEY_CACHE_NAME            = "officeonline.key.Cache".intern();

  /** The Constant SECRET_KEY. */
  protected static final String          SECRET_KEY                = "secret-key";

  /** The Constant ALGORITHM. */
  protected static final String          ALGORITHM                 = "AES";

  /** The Constant TOKEN_DELIMITER. */
  protected static final String          TOKEN_DELIMITER           = "+";

  /** The Constant TOKEN_DELIMITER_PATTERN. */
  protected static final String          TOKEN_DELIMITER_PATTERN   = "\\+";

  /** The Constant TOKEN_EXPIRES. */
  protected static final long            TOKEN_EXPIRES             = 30 * 60000;

  /** The Constant JCR_CONTENT. */
  protected static final String          JCR_CONTENT               = "jcr:content";

  /** The Constant WOPITESTX. */
  protected static final String          WOPITESTX                 = "wopitestx";

  /** The Constant WOPITEST. */
  protected static final String          WOPITEST                  = "wopitest";

  /** The Constant JCR_DATA. */
  protected static final String          JCR_DATA                  = "jcr:data";

  /** The Constant EXO_LAST_MODIFIER. */
  protected static final String          EXO_LAST_MODIFIER         = "exo:lastModifier";

  /** The Constant EXO_LAST_MODIFIED_DATE. */
  protected static final String          EXO_LAST_MODIFIED_DATE    = "exo:lastModifiedDate";

  /** The Constant EXO_DATE_MODIFIED. */
  protected static final String          EXO_DATE_MODIFIED         = "exo:dateModified";

  /** The Constant JCR_LAST_MODIFIED. */
  protected static final String          JCR_LAST_MODIFIED         = "jcr:lastModified";

  /** The Constant MIX_VERSIONABLE. */
  protected static final String          MIX_VERSIONABLE           = "mix:versionable";

  /** The Constant EXO_OWNER. */
  protected static final String          EXO_OWNER                 = "exo:owner";

  /** The Constant EXO_TITLE. */
  protected static final String          EXO_TITLE                 = "exo:title";

  /** The Constant EXO_PRIVILEGEABLE. */
  protected static final String          EXO_PRIVILEGEABLE         = "exo:privilegeable";

  /** The Constant JCR_MIME_TYPE. */
  protected static final String          JCR_MIME_TYPE             = "jcr:mimeType";

  /** The Constant EXO_NAME. */
  protected static final String          EXO_NAME                  = "exo:name";

  /** The Constant PATH. */
  protected static final String          PATH                      = "path";

  /** The Constant MSOFFICE_USER_PREFERENCES. */
  protected static final String          MSOFFICE_USER_PREFERENCES = "msoffice:userPreferences";

  /** The Constant MSOFFICE_FILE. */
  protected static final String          MSOFFICE_FILE             = "msoffice:file";

  /** The Constant MSOFFICE_PREFERENCES. */
  protected static final String          MSOFFICE_PREFERENCES      = "msoffice:preferences";

  /** Cache of Editing documents. */
  protected final ExoCache<String, Key>  keyCache;

  /** The session providers. */
  protected final SessionProviderService sessionProviders;

  /** The user ACL. */
  protected final UserACL                userACL;

  /** The jcr service. */
  protected final RepositoryService      jcrService;

  /** The organization. */
  protected final OrganizationService    organization;

  /** The document service. */
  protected final DocumentService        documentService;

  /** The identity registry. */
  protected final IdentityRegistry       identityRegistry;

  /** The authenticator. */
  protected final Authenticator          authenticator;

  /** The node finder. */
  protected NodeFinder                   nodeFinder;

  /**
   * Instantiates a new abstract office online service.
   *
   * @param sessionProviders the session providers
   * @param jcrService the jcr service
   * @param organization the organization
   * @param documentService the document service
   * @param cacheService the cache service
   * @param userACL the user ACL
   * @param identityRegistry the identity registry
   * @param authenticator the authenticator
   * @param nodeFinder the node finder
   */
  public AbstractOfficeOnlineService(SessionProviderService sessionProviders,
                                     RepositoryService jcrService,
                                     OrganizationService organization,
                                     DocumentService documentService,
                                     CacheService cacheService,
                                     UserACL userACL,
                                     IdentityRegistry identityRegistry,
                                     Authenticator authenticator,
                                     NodeFinder nodeFinder) {
    this.sessionProviders = sessionProviders;
    this.jcrService = jcrService;
    this.organization = organization;
    this.documentService = documentService;
    this.keyCache = cacheService.getCacheInstance(KEY_CACHE_NAME);
    this.userACL = userACL;
    this.identityRegistry = identityRegistry;
    this.authenticator = authenticator;
    this.nodeFinder = nodeFinder;
  }

  /**
   * Node by UUID.
   *
   * @param uuid the uuid
   * @param workspace the workspace
   * @return the node
   * @throws FileNotFoundException the file not found exception
   * @throws RepositoryException the repository exception
   */
  public Node nodeByUUID(String uuid, String workspace) throws FileNotFoundException, RepositoryException {
    try {
      Session userSession = getUserSession(workspace);
      return userSession.getNodeByUUID(uuid);
    } catch (ItemNotFoundException e) {
      LOG.warn("Cannot find node by UUID: {}, workspace: {}. Error: {}", uuid, workspace, e.getMessage());
      throw new FileNotFoundException("File not found. FileId: " + uuid + ", workspace: " + workspace);
    }
  }

  /**
   * Gets the user session.
   *
   * @param workspace the workspace
   * @return the user session
   * @throws RepositoryException the repository exception
   */
  protected Session getUserSession(String workspace) throws RepositoryException {
    if (workspace == null) {
      workspace = jcrService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
    }
    SessionProvider sp = sessionProviders.getSessionProvider(null);
    return sp.getSession(workspace, jcrService.getCurrentRepository());
  }

  /**
   * Node content.
   *
   * @param node the node
   * @return the node
   * @throws RepositoryException the repository exception
   */
  protected Node nodeContent(Node node) throws RepositoryException {
    return node.getNode("jcr:content");
  }

  /**
   * Gets the content.
   *
   * @param config the config
   * @return the content
   * @throws OfficeOnlineException the office online exception
   */
  public DocumentContent getContent(EditorConfig config) throws OfficeOnlineException {
    try {
      Node node = nodeByUUID(config.getFileId(), config.getWorkspace());
      Node content = nodeContent(node);

      final String mimeType = content.getProperty(JCR_MIME_TYPE).getString();
      // data stream will be closed when EoF will be reached
      final InputStream data = new AutoCloseInputStream(content.getProperty(JCR_DATA).getStream());
      final String filename = nodeTitle(node);

      return new DocumentContent() {

        @Override
        public String getFilename() {
          return filename;
        }

        @Override
        public String getType() {
          return mimeType;
        }

        @Override
        public InputStream getData() {
          return data;
        }

        @Override
        public String getVersion() throws RepositoryException {
          return node.isNodeType(MIX_VERSIONABLE) ? node.getBaseVersion().getName() : null;
        }
      };
    } catch (RepositoryException e) {
      LOG.error("Cannot get content of node. FileId: " + config.getFileId(), e.getMessage());
      throw new OfficeOnlineException("Cannot get file content. FileId: " + config.getFileId());
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
   * Platform REST URL.
   *
   * @param platformUrl the platform URL
   * @return the string builder
   */
  protected StringBuilder platformRestUrl(CharSequence platformUrl) {
    StringBuilder restUrl = new StringBuilder(platformUrl);
    restUrl.append('/');
    restUrl.append(PortalContainer.getCurrentRestContextName());

    return restUrl;
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
   * Explorer uri.
   *
   * @param baseUrl the base url
   * @param ecmsLink the ecms link
   * @return the uri
   */
  protected URI explorerUri(String baseUrl, String ecmsLink) {
    URI uri;
    try {
      URI baseURI = new URI(baseUrl);
      ecmsLink = URLDecoder.decode(ecmsLink, StandardCharsets.UTF_8.name());
      String[] linkParts = ecmsLink.split("\\?");
      if (linkParts.length >= 2) {
        uri = new URI(baseURI.getScheme(), null, baseURI.getHost(), baseURI.getPort(), linkParts[0], linkParts[1], null);
      } else {
        uri = new URI(baseURI.getScheme(), null, baseURI.getHost(), baseURI.getPort(), ecmsLink, null, null);
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
   * Creates the editor config.
   *
   * @param userId the userId
   * @param fileId the file id
   * @param workspace the workspace
   * @param requestInfo the request info
   * @return the editor config
   * @throws OfficeOnlineException the office online exception
   * @throws RepositoryException the repository exception
   */
  public EditorConfig createEditorConfig(String userId,
                                         String fileId,
                                         String workspace,
                                         RequestInfo requestInfo) throws OfficeOnlineException, RepositoryException {

    Node node = nodeByUUID(fileId, workspace);
    List<Permissions> permissions = new ArrayList<>();
    if (PermissionUtil.canSetProperty(node)) {
      permissions.add(Permissions.USER_CAN_WRITE);
      permissions.add(Permissions.USER_CAN_RENAME);
    } else {
      permissions.add(Permissions.READ_ONLY);
    }
    String baseUrl = platformUrl(requestInfo.getScheme(), requestInfo.getServerName(), requestInfo.getPort()).toString();
    EditorConfig.Builder configBuilder = new EditorConfig.Builder().userId(userId)
                                                                   .fileId(fileId)
                                                                   .workspace(workspace)
                                                                   .permissions(permissions)
                                                                   .baseUrl(baseUrl);
    AccessToken accessToken = generateAccessToken(configBuilder);
    configBuilder.accessToken(accessToken);
    return configBuilder.build();
  }

  /**
   * Generate access token.
   *
   * @param configBuilder the config builder
   * @return the string
   * @throws OfficeOnlineException the office online exception
   */
  public AccessToken generateAccessToken(EditorConfig.Builder configBuilder) throws OfficeOnlineException {
    try {
      Key key = keyCache.get(SECRET_KEY);
      Cipher chiper = Cipher.getInstance(ALGORITHM);
      chiper.init(Cipher.ENCRYPT_MODE, key);

      long expires = System.currentTimeMillis() + TOKEN_EXPIRES;

      StringBuilder builder = new StringBuilder().append(configBuilder.workspace())
                                                 .append(TOKEN_DELIMITER)
                                                 .append(configBuilder.userId())
                                                 .append(TOKEN_DELIMITER)
                                                 .append(configBuilder.fileId())
                                                 .append(TOKEN_DELIMITER)
                                                 .append(expires)
                                                 .append(TOKEN_DELIMITER)
                                                 .append(configBuilder.baseUrl());
      configBuilder.permissions().forEach(permission -> {
        builder.append(TOKEN_DELIMITER).append(permission.getShortName());
      });
      byte[] encrypted = chiper.doFinal(builder.toString().getBytes());
      String token = new String(Base64.getUrlEncoder().encode(encrypted));
      return new AccessToken(token, expires);
    } catch (Exception e) {
      LOG.error("Error occured while generating token. {}", e.getMessage());
      throw new OfficeOnlineException("Couldn't generate token from editor config.");
    }
  }

  /**
   * Gets the node.
   *
   * @param workspace the workspace
   * @param path the path
   * @return the node
   * @throws RepositoryException the repository exception
   */
  public Node getNode(String workspace, String path) throws RepositoryException {
    if (workspace == null) {
      workspace = jcrService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
    }
    SessionProvider sp = sessionProviders.getSessionProvider(null);
    Session userSession = sp.getSession(workspace, jcrService.getCurrentRepository());
    Item item = nodeFinder.getItem(userSession, path, true);
    if (item != null && item.isNode()) {
      return (Node) item;
    }
    return null;
  }

  /**
   * Builds the editor config.
   *
   * @param token the access token
   * @return the editor config
   * @throws OfficeOnlineException the office online exception
   */
  public EditorConfig buildEditorConfig(String token) throws OfficeOnlineException {
    String decryptedToken = "";
    try {
      Key key = keyCache.get(SECRET_KEY);
      Cipher chiper = Cipher.getInstance(ALGORITHM);
      chiper.init(Cipher.DECRYPT_MODE, key);
      byte[] decoded = Base64.getUrlDecoder().decode(token.getBytes());
      decryptedToken = new String(chiper.doFinal(decoded));
    } catch (Exception e) {
      LOG.error("Error occured while decoding/decrypting accessToken. {}", e.getMessage());
      throw new OfficeOnlineException("Cannot decode/decrypt accessToken");
    }

    List<Permissions> permissions = new ArrayList<>();
    List<String> values = Arrays.asList(decryptedToken.split(TOKEN_DELIMITER_PATTERN));
    if (values.size() > 4) {
      String workspace = values.get(0);
      if (workspace.equals("null")) {
        workspace = null;
      }
      String userId = values.get(1);
      String fileId = values.get(2);
      long expires = Long.parseLong(values.get(3));
      String baseUrl = values.get(4);
      values.stream().skip(5).forEach(value -> permissions.add(Permissions.fromShortName(value)));
      return new EditorConfig.Builder().userId(userId)
                                       .fileId(fileId)
                                       .workspace(workspace)
                                       .baseUrl(baseUrl)
                                       .permissions(permissions)
                                       .accessToken(new AccessToken(token, expires))
                                       .build();
    } else {
      throw new OfficeOnlineException("Decrypted token doesn't contain all required parameters");
    }
  }

  /**
   * Sets ConversationState by userId.
   *
   * @param userId the userId
   * @return true if successful, false when the user is not found
   */
  @SuppressWarnings("deprecation")

  protected boolean setUserConvoState(String userId) {
    Identity userIdentity = userIdentity(userId);
    if (userIdentity != null) {
      ConversationState state = new ConversationState(userIdentity);
      // Keep subject as attribute in ConversationState.
      state.setAttribute(ConversationState.SUBJECT, userIdentity.getSubject());
      ConversationState.setCurrent(state);
      SessionProvider userProvider = new SessionProvider(state);
      sessionProviders.setSessionProvider(null, userProvider);
      return true;
    }
    LOG.warn("User identity not found " + userId + " for setting conversation state");
    return false;
  }

  /**
   * Restores the conversation state.
   * 
   * @param contextState the contextState
   * @param contextProvider the contextProvider
   */
  protected void restoreConvoState(ConversationState contextState, SessionProvider contextProvider) {
    ConversationState.setCurrent(contextState);
    sessionProviders.setSessionProvider(null, contextProvider);
  }

  /**
   * Find or create user identity.
   *
   * @param userId the user id
   * @return the identity can be null if not found and cannot be created via
   *         current authenticator
   */
  protected Identity userIdentity(String userId) {
    Identity userIdentity = identityRegistry.getIdentity(userId);
    if (userIdentity == null) {
      // We create user identity by authenticator, but not register it in the
      // registry
      try {
        if (LOG.isDebugEnabled()) {
          LOG.debug("User identity not registered, trying to create it for: " + userId);
        }
        userIdentity = authenticator.createIdentity(userId);
      } catch (Exception e) {
        LOG.warn("Failed to create user identity: " + userId, e);
      }
    }
    return userIdentity;
  }

  /**
   * Addds file preferences to the node (path for opening shared doc for particular user).
   * @param node the node
   * @param userId the userId
   * @param path the path
   * @throws RepositoryException  the repositoryException
   */
  public void addFilePreferences(Node node, String userId, String path) throws RepositoryException {
    if (node.canAddMixin(MSOFFICE_FILE)) {
      addMixin(node, MSOFFICE_FILE);
    }
    NodeImpl preferences = (NodeImpl) node.getNode(MSOFFICE_PREFERENCES);
    Node userPreferences;
    if (!preferences.hasNode(userId)) {
      userPreferences = preferences.addNode(userId, MSOFFICE_USER_PREFERENCES);
    } else {
      userPreferences = preferences.getNode(userId);
    }
    userPreferences.setProperty(PATH, path);
    if (!preferences.hasPermission(PermissionType.ADD_NODE)) {
      addWritePermissions(preferences, userId);
    }
    node.save();
  }
  
  /**
   * Node title.
   *
   * @param node the node
   * @return the string
   * @throws RepositoryException the repository exception
   */
  public String nodeTitle(Node node) throws RepositoryException {
    String title = null;
    if (node.hasProperty("exo:title")) {
      title = node.getProperty("exo:title").getString();
    } else if (node.hasProperty("jcr:content/dc:title")) {
      Property dcTitle = node.getProperty("jcr:content/dc:title");
      if (dcTitle.getDefinition().isMultiple()) {
        Value[] dctValues = dcTitle.getValues();
        if (dctValues.length > 0) {
          title = dctValues[0].getString();
        }
      } else {
        title = dcTitle.getString();
      }
    } else if (node.hasProperty("exo:name")) {
      // FYI exo:name seems the same as node name
      title = node.getProperty("exo:name").getString();
    }
    if (title == null) {
      title = node.getName();
    }
    try {
      title = URLDecoder.decode(title, UTF_8);
    } catch (UnsupportedEncodingException e) {
      LOG.warn("Cannot decode node name using URLDecoder. {}", e.getMessage());
    }
    return title;
  }

  /**
   * Adds the mixin.
   *
   * @param node the node
   * @param mixin the mixin
   * @throws RepositoryException the repository exception
   */
  private void addMixin(Node node, String mixin) throws RepositoryException {
    Session systemSession = jcrService.getCurrentRepository().getSystemSession(node.getSession().getWorkspace().getName());
    NodeImpl systemNode = (NodeImpl) systemSession.getItem(node.getPath());
    systemNode.addMixin(mixin);
    systemNode.save();
  }

  /**
   * Adds the write permissions.
   *
   * @param node the node
   * @param userId the user id
   * @throws RepositoryException the repository exception
   */
  private void addWritePermissions(Node node, String userId) throws RepositoryException {
    Session systemSession = jcrService.getCurrentRepository().getSystemSession(node.getSession().getWorkspace().getName());
    NodeImpl systemNode = (NodeImpl) systemSession.getItem(node.getPath());
    systemNode.addMixin(EXO_PRIVILEGEABLE);
    systemNode.setPermission(userId, new String[] { PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY });
    systemNode.save();
  }

  /**
   * Gets parent folder of the file based on file preferences.
   *
   * @param node the node
   * @param userId the userId
   * @return the Node
   * @throws RepositoryException the repository exception
   */
  protected Node getSymlink(Node node, String userId) throws RepositoryException {
    if (node.hasNode(MSOFFICE_PREFERENCES)) {
      Node filePreferences = node.getNode(MSOFFICE_PREFERENCES);
      if (filePreferences.hasNode(userId)) {
        Node userPreferences = filePreferences.getNode(userId);
        String path = userPreferences.getProperty(PATH).getString();
        return (Node) node.getSession().getItem(path);
      }
    }
    return null;
  }
 
}
