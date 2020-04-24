/*
 * Copyright (C) 2003-2020 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.officeonline.documents;

import static org.exoplatform.officeonline.webui.OfficeOnlineContext.callModule;

import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.ecm.webui.component.explorer.UIJCRExplorer;
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.officeonline.cometd.CometdConfig;
import org.exoplatform.officeonline.cometd.CometdOfficeOnlineService;
import org.exoplatform.officeonline.exception.FileNotFoundException;
import org.exoplatform.services.cms.documents.DocumentEditor;
import org.exoplatform.services.cms.documents.NewDocumentTemplate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * The Class OnlyOfficeNewDocumentEditorPlugin.
 */
public class OfficeOnlineDocumentEditorPlugin extends BaseComponentPlugin implements DocumentEditor {

  /** The Constant PROVIDER_NAME. */
  protected static final String             PROVIDER_NAME                = "officeonline";

  /** The Constant PROVIDER_CONFIGURATION_PARAM. */
  protected static final String             PROVIDER_CONFIGURATION_PARAM = "provider-configuration";

  /** The Constant CLIENT_RESOURCE_PREFIX. */
  protected static final String             CLIENT_RESOURCE_PREFIX       = "OfficeOnlineEditorClient.";

  /** The Constant LOG. */
  protected static final Log                LOG                          =
                                                ExoLogger.getLogger(OfficeOnlineDocumentEditorPlugin.class);

  /** The wopi service. */
  protected final WOPIService               wopiService;

  /** The i 18 n service. */
  protected final ResourceBundleService     i18nService;

  /** The cometd service. */
  protected final CometdOfficeOnlineService cometdService;

  /** The editor links. */
  protected final Map<Node, String>         editorLinks                  = new ConcurrentHashMap<>();

  /**
   * Instantiates a new office online document editor plugin.
   *
   * @param wopiService the wopi service
   * @param i18nService the i 18 n service
   * @param cometdService the cometd service
   */
  public OfficeOnlineDocumentEditorPlugin(WOPIService wopiService,
                                          ResourceBundleService i18nService,
                                          CometdOfficeOnlineService cometdService) {
    this.wopiService = wopiService;
    this.i18nService = i18nService;
    this.cometdService = cometdService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onDocumentCreated(String workspace, String path) throws Exception {
    Node node = wopiService.getNode(workspace, path);
    String link = wopiService.getEditorLink(node, WOPIService.EDIT_ACTION);
    if (link != null) {
      link = new StringBuilder().append("'").append(link).append("'").toString();
    } else {
      link = "null";
      LOG.error("Cannot get editor link for document: {}, workspace: {}", path, workspace);
    }
    callModule("officeonline.initEditorPage(" + link + ");");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void beforeDocumentCreate(NewDocumentTemplate template, String parentPath, String title) throws Exception {
    callModule("officeonline.initNewDocument();");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getProviderName() {
    return PROVIDER_NAME;
  }

  /**
   * Inits the activity.
   *
   * @param uuid the uuid
   * @param workspace the workspace
   * @param activityId the activity id
   * @throws Exception the exception
   */
  @Override
  public void initActivity(String uuid, String workspace, String activityId) throws Exception {
    Node symlink = wopiService.nodeByUUID(uuid, workspace);
    Node node = wopiService.getNode(symlink.getSession().getWorkspace().getName(), symlink.getPath());
    if (node != null) {
      String link = getEditorLink(node);
      if (link != null) {
        link = new StringBuilder("'").append(link).append("'").toString();
      } else {
        link = "null";
      }
      callModule("officeonline.initActivity('" + node.getUUID() + "', " + link + ", '" + activityId + "');");
    }
  }

  /**
   * Inits the preview.
   *
   * @param fileId the uuid
   * @param workspace the workspace
   * @param requestURI the requestURI
   * @param locale the locale
   * @return the editor settings
   */
  @SuppressWarnings("unchecked")
  @Override
  public EditorSetting initPreview(String fileId, String workspace, URI requestURI, Locale locale) {
    try {
      String userId = ConversationState.getCurrent().getIdentity().getUserId();
      Node symlink = wopiService.nodeByUUID(fileId, workspace);
      Node node = wopiService.getNode(symlink.getSession().getWorkspace().getName(), symlink.getPath());
      if (node != null) {
        if (symlink.isNodeType("exo:symlink")) {
          wopiService.addFilePreferences(node, userId, symlink.getPath());
        }
        String link = getEditorLink(node, requestURI);
        Map<String, String> messages = initMessages(locale);
        CometdConfig cometdConf = new CometdConfig(cometdService.getCometdServerPath(),
                                                   cometdService.getUserToken(userId),
                                                   PortalContainer.getCurrentPortalContainerName());
        return new EditorSetting(fileId, link, userId, cometdConf, messages);
      }
    } catch (FileNotFoundException e) {
      LOG.error("Cannot initialize preview for fileId: {}, workspace: {}. {}", fileId, workspace, e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("Cannot initialize preview", e);
    }
    return null;
  }

  /**
   * Inits the explorer.
   *
   * @param fileId the file id
   * @param workspace the workspace
   * @param context the context
   * @return the editor setting
   */
  @SuppressWarnings("unchecked")
  @Override
  public EditorSetting initExplorer(String fileId, String workspace, WebuiRequestContext context) {
    try {
      Node node = wopiService.nodeByUUID(fileId, workspace);
      node = wopiService.getNode(node.getSession().getWorkspace().getName(), node.getPath());

      // Handling symlinks
      UIJCRExplorer uiExplorer = context.getUIApplication().findFirstComponentOfType(UIJCRExplorer.class);
      Node symlink = (Node) uiExplorer.getSession().getItem(uiExplorer.getCurrentPath());
      if (symlink.isNodeType("exo:symlink")) {
        wopiService.addFilePreferences(node, WebuiRequestContext.getCurrentInstance().getRemoteUser(), symlink.getPath());
      }

      if (wopiService.isDocumentSupported(node)) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Init documents explorer for node: {}:{}", workspace, fileId);
        }
        String userId = context.getRemoteUser();
        String editorLink = null;
        if (wopiService.canEdit(node)) {
          editorLink = wopiService.getEditorLink(node, WOPIService.EDIT_ACTION);
        } else if (wopiService.canView(node)) {
          editorLink = wopiService.getEditorLink(node, WOPIService.VIEW_ACTION);
        }
        Map<String, String> messages = initMessages(WebuiRequestContext.getCurrentInstance().getLocale());
        CometdConfig cometdConf = new CometdConfig(cometdService.getCometdServerPath(),
                                                   cometdService.getUserToken(userId),
                                                   PortalContainer.getCurrentPortalContainerName());
        return new EditorSetting(fileId, editorLink, userId, cometdConf, messages);
      }
    } catch (Exception e) {
      LOG.error("Cannot initialize exlporer for fileId: {}, workspace: {}. {}", fileId, workspace, e.getMessage());
    }
    return null;
  }

  /**
   * Checks if is document supported.
   *
   * @param fileId the file id
   * @param workspace the workspace
   * @return true, if is document supported
   */
  @Override
  public boolean isDocumentSupported(String fileId, String workspace) {
    Node node;
    try {
      node = wopiService.nodeByUUID(fileId, workspace);
      return wopiService.canEdit(node) || wopiService.canView(node);
    } catch (FileNotFoundException e) {
      LOG.error("Cannot check if file is suported, document not found. {}", e.getMessage());
    } catch (RepositoryException e) {
      LOG.error("Cannot check if file is suported", e);
    }
    return false;
  }

  /**
   * Returns editor link, adds it to the editorLinks cache.
   *
   * @param node the node
   * @param requestURI the request URI
   * @return the string
   */
  protected String getEditorLink(Node node, URI requestURI) {
    String link = editorLinks.computeIfAbsent(node, n -> {
      if (wopiService.canEdit(node)) {
        return wopiService.getEditorLink(node, requestURI, WOPIService.EDIT_ACTION);
      } else if (wopiService.canView(node)) {
        return wopiService.getEditorLink(node, requestURI, WOPIService.VIEW_ACTION);
      }
      return null;
    });
    return link;
  }

  /**
   * Returns editor link, adds it to the editorLinks cache.
   *
   * @param node the node
   * @return the string
   */
  protected String getEditorLink(Node node) {
    String link = editorLinks.computeIfAbsent(node, n -> {
      if (wopiService.canEdit(node)) {
        return wopiService.getEditorLink(node, WOPIService.EDIT_ACTION);
      } else if (wopiService.canView(node)) {
        return wopiService.getEditorLink(node, WOPIService.VIEW_ACTION);
      }
      return null;
    });
    return link;
  }

  /**
   * Inits the messages.
   *
   * @param locale the locale
   * @return the map
   */
  private Map<String, String> initMessages(Locale locale) {
    ResourceBundle res = i18nService.getResourceBundle("locale.officeonline.OfficeOnlineClient", locale);
    Map<String, String> messages = new HashMap<String, String>();
    for (Enumeration<String> keys = res.getKeys(); keys.hasMoreElements();) {
      String key = keys.nextElement();
      String bundleKey;
      if (key.startsWith(CLIENT_RESOURCE_PREFIX)) {
        bundleKey = key.substring(CLIENT_RESOURCE_PREFIX.length());
      } else {
        bundleKey = key;
      }
      messages.put(bundleKey, res.getString(key));
    }
    return messages;
  }

  /**
   * The Class EditorSetting.
   */
  protected static class EditorSetting {

    /** The file id. */
    private final String              fileId;

    /** The link. */
    private final String              link;

    /** The user id. */
    private final String              userId;

    /** The cometd conf. */
    private final CometdConfig        cometdConf;

    /** The messages. */
    private final Map<String, String> messages;

    /**
     * Instantiates a new editor setting.
     *
     * @param fileId the file id
     * @param link the link
     * @param userId the user id
     * @param cometdConf the cometd conf
     * @param messages the messages
     */
    public EditorSetting(String fileId, String link, String userId, CometdConfig cometdConf, Map<String, String> messages) {
      this.fileId = fileId;
      this.link = link;
      this.userId = userId;
      this.cometdConf = cometdConf;
      this.messages = messages;
    }

    /**
     * Gets the file id.
     *
     * @return the file id
     */
    public String getFileId() {
      return fileId;
    }

    /**
     * Gets the link.
     *
     * @return the link
     */
    public String getLink() {
      return link;
    }

    /**
     * Gets the user id.
     *
     * @return the user id
     */
    public String getUserId() {
      return userId;
    }

    /**
     * Gets the cometd conf.
     *
     * @return the cometd conf
     */
    public CometdConfig getCometdConf() {
      return cometdConf;
    }

    /**
     * Gets the messages.
     *
     * @return the messages
     */
    public Map<String, String> getMessages() {
      return messages;
    }
  }
}
