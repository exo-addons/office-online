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

import java.io.InputStream;
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

import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.ecm.webui.utils.Utils;
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
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class OfficeOnlineEditorServiceImpl.
 */
public class OfficeOnlineEditorService implements Startable {


  /**
   * Start.
   */
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
      List<String> permissions = new ArrayList<>();

      if (document != null) {
        if (canEditDocument(document)) {
          permissions.add(USER_CAN_WRITE);
          permissions.add(USER_CAN_RENAME);
        } else {
          permissions.add(READ_ONLY);
        }
      }

      config = new EditorConfig(userId, fileId, workspace, permissions);
      String accessToken = idGenerator.generateStringID(config);
      config.setAccessToken(accessToken);
      configs.put(accessToken, config);
    } finally {
      restoreConvoState(contextState, contextProvider);
    }
    return config;
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
    EditorConfig config = configs.get(accessToken);
    if (config != null) {
      if (config.getUserId().equals(userId) && config.getFileId().equals(fileId)) {
        ConversationState contextState = ConversationState.getCurrent();
        SessionProvider contextProvider = sessionProviders.getSessionProvider(null);
        try {
          // We all the job under actual (requester) user here
          if (!setUserConvoState(userId)) {
            LOG.error("Couldn't set user conversation state. UserId: {}", userId);
            throw new OfficeOnlineException("Cannot set conversation state " + userId);
          }
          // work in user session
          Node node = nodeByUUID(fileId, config.getWorkspace());
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
      } else {
        throw new OfficeOnlineException("Access token doesn't match user or file. UserId: " + userId + ", fileId: " + fileId
            + ", token: " + accessToken);
      }
    } else {
      throw new OfficeOnlineException("Access token not found: " + accessToken);
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
   * Sets the plugin.
   *
   * @param plugin the plugin
   */
  public void setWOPIServicePlugin(ComponentPlugin plugin) {
    Class<WOPIService> pclass = WOPIService.class;
    if (pclass.isAssignableFrom(plugin.getClass())) {
      wopiService = pclass.cast(plugin);
      LOG.info("Set WOPIService instance of " + plugin.getClass().getName());
    } else {
      throw new WopiDiscoveryNotFoundException("WopiDiscoveryPlugin is not an instance of " + pclass.getName());
    }
  }

  /**
   * Gets the WOPI service.
   *
   * @return the WOPI service
   */
  public WOPIService getWOPIService() {
    return wopiService;
  }


}
