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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.officeonline.OfficeOnlineDocumentUpdateActivityHandler;
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.services.cms.documents.DocumentEditorPlugin;
import org.exoplatform.services.cms.documents.DocumentTemplate;
import org.exoplatform.services.cms.documents.DocumentUpdateActivityHandler;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * The Class OnlyOfficeNewDocumentEditorPlugin.
 */
public class OfficeOnlineDocumentEditorPlugin extends BaseComponentPlugin implements DocumentEditorPlugin {

  /** The Constant PROVIDER_NAME. */
  protected static final String     PROVIDER_NAME = "OfficeOnline";

  /** The Constant LOG. */
  protected static final Log        LOG           = ExoLogger.getLogger(OfficeOnlineDocumentEditorPlugin.class);

  /** The wopi service. */
  protected final WOPIService       wopiService;

  /** The editor links. */
  protected final Map<Node, String> editorLinks   = new ConcurrentHashMap<>();
  
  protected final OfficeOnlineDocumentUpdateActivityHandler updateHandler;

  /**
   * Instantiates a new office online new document editor plugin.
   *
   * @param wopiService the wopi service
   */
  public OfficeOnlineDocumentEditorPlugin(WOPIService wopiService) {
    this.wopiService = wopiService;
    this.updateHandler = new OfficeOnlineDocumentUpdateActivityHandler();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onDocumentCreated(String workspace, String path) throws Exception {
    Node node = wopiService.getNode(workspace, path);
    String link = wopiService.getEditorLink(node, WOPIService.EDIT_ACTION);
    link = link != null ? new StringBuilder().append("'").append(link).append("'").toString() : "null".intern();
    callModule("officeonline.initEditorPage(" + link + ");");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void beforeDocumentCreate(DocumentTemplate template, String parentPath, String title) throws Exception  {
    callModule("officeonline.initNewDocument();");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getProviderName() {
    return PROVIDER_NAME;
  }

  @Override
  public void initActivity(String uuid, String workspace, String activityId, String context) throws Exception {
    Node symlink = wopiService.nodeByUUID(uuid, workspace);
    Node node = wopiService.getNode(symlink.getSession().getWorkspace().getName(), symlink.getPath());
    if (node != null) {
      String link = getEditorLink(node);
      callModule("officeonline.initActivity('" + node.getUUID() + "', " + link + ", '" + activityId + "');");
    }
  }

  @Override
  public void initPreview(String uuid, String workspace, String activityId, String context, int index) throws Exception {
    Node symlink = wopiService.nodeByUUID(uuid, workspace);
    Node node = wopiService.getNode(symlink.getSession().getWorkspace().getName(), symlink.getPath());
    if (node != null) {
      if (symlink.isNodeType("exo:symlink")) {
        String userId = WebuiRequestContext.getCurrentInstance().getRemoteUser();
        wopiService.addFilePreferences(node, userId, symlink.getPath());
      }
      String link = getEditorLink(node);
      callModule("officeonline.initPreview('" + node.getUUID() + "', " + link + ", '" + activityId + "', '" + index + "');");
    }

  }
  
  @Override
  public DocumentUpdateActivityHandler getDocumentUpdateHandler() {
    return updateHandler;
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
    link = link != null ? new StringBuilder().append("'").append(link).append("'").toString() : "null".intern();
    return link;
  }

}
