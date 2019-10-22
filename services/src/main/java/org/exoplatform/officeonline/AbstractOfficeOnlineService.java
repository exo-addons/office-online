package org.exoplatform.officeonline;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.picocontainer.Startable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
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
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

/**
 * The Class AbstractOfficeOnlineService.
 */
public abstract class AbstractOfficeOnlineService implements Startable {

  /** The Constant LOG. */
  protected static final Log             LOG                  = ExoLogger.getLogger(AbstractOfficeOnlineService.class);

  /** The Constant CACHE_NAME. */
  public static final String             CACHE_NAME           = "officeonline.Cache".intern();

  /** The Constant SECRET_KEY. */
  protected static final String          SECRET_KEY           = "secret-key";

  /** The Constant ALGORITHM. */
  protected static final String          ALGORITHM            = "AES";

  /** The Constant TOKEN_DELIMITER. */
  protected static final String          TOKEN_DELIMITER      = "+";

  /** The Constant TOKEN_DELIMITER_SPLIT. */
  protected static final String          TOKEN_DELIMITE_SPLIT = "\\+";

  /** Cache of Editing documents. */
  protected final ExoCache<String, Key>  activeCache;

  /** The generator. */
  protected final IDGeneratorService     idGenerator;

  /** The session providers. */
  protected final SessionProviderService sessionProviders;

  /** The authenticator. */
  protected final Authenticator          authenticator;

  /** The identity registry. */
  protected final IdentityRegistry       identityRegistry;

  /** The jcr service. */
  protected final RepositoryService      jcrService;

  /** The organization. */
  protected final OrganizationService    organization;

  /** The document service. */
  protected final DocumentService        documentService;

  /**
   * Instantiates a new office online editor service.
   *
   * @param sessionProviders the session providers
   * @param idGenerator the id generator
   * @param jcrService the jcr service
   * @param organization the organization
   * @param documentService the document service
   * @param authenticator the authenticator
   * @param identityRegistry the identity registry
   * @param cacheService the cache service
   */
  public AbstractOfficeOnlineService(SessionProviderService sessionProviders,
                                     IDGeneratorService idGenerator,
                                     RepositoryService jcrService,
                                     OrganizationService organization,
                                     DocumentService documentService,
                                     Authenticator authenticator,
                                     IdentityRegistry identityRegistry,
                                     CacheService cacheService) {
    this.sessionProviders = sessionProviders;
    this.idGenerator = idGenerator;
    this.jcrService = jcrService;
    this.organization = organization;
    this.documentService = documentService;
    this.authenticator = authenticator;
    this.identityRegistry = identityRegistry;
    this.activeCache = cacheService.getCacheInstance(CACHE_NAME);
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
   * Generate access token.
   *
   * @param config the config
   * @return the string
   */
  protected String generateAccessToken(EditorConfig config) {
    try {
      Key key = activeCache.get(SECRET_KEY);
      Cipher chiper = Cipher.getInstance(ALGORITHM);
      chiper.init(Cipher.ENCRYPT_MODE, key);
      StringBuilder builder = new StringBuilder().append(config.getWorkspace())
                                                 .append(TOKEN_DELIMITER)
                                                 .append(config.getUserId())
                                                 .append(TOKEN_DELIMITER)
                                                 .append(config.getFileId());
      config.getPermissions().forEach(permission -> {
        builder.append(TOKEN_DELIMITER).append(permission.getShortName());
      });
      byte[] encrypted = chiper.doFinal(builder.toString().getBytes());
      return new String(Base64.getUrlEncoder().encode(encrypted));
    } catch (Exception e) {
      LOG.error("Cannot generate access token. {}", e.getMessage());
    }
    return null;
  }

  /**
   * Builds the editor config.
   *
   * @param accessToken the access token
   * @return the editor config
   * @throws OfficeOnlineException the office online exception
   */
  protected EditorConfig buildEditorConfig(String accessToken) throws OfficeOnlineException {
    String decryptedToken = "";
    try {
      Key key = activeCache.get(SECRET_KEY);
      Cipher chiper = Cipher.getInstance(ALGORITHM);
      chiper.init(Cipher.DECRYPT_MODE, key);
      byte[] decoded = Base64.getUrlDecoder().decode(accessToken.getBytes());
      decryptedToken = new String(chiper.doFinal(decoded));
    } catch (Exception e) {
      LOG.error("Error occured while decoding/decrypting accessToken. {}", e.getMessage());
      throw new OfficeOnlineException("Cannot decode/decrypt accessToken");
    }

    List<Permissions> permissions = new ArrayList<>();
    List<String> values = Arrays.asList(decryptedToken.split(TOKEN_DELIMITE_SPLIT));
    if (values.size() > 2) {
      String workspace = values.get(0);
      String userId = values.get(1);
      String fileId = values.get(2);
      values.stream().skip(3).forEach(value -> permissions.add(Permissions.fromShortName(value)));
      return new EditorConfig(userId, fileId, workspace, permissions, accessToken);
    } else {
      throw new OfficeOnlineException("Decrypted token doesn't contain all required parameters");
    }
  }

}
