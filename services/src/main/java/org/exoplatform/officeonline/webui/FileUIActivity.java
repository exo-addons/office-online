/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.officeonline.webui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;

import org.exoplatform.officeonline.EditorService;
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

/**
 * Created by The eXo Platform SAS.
 *
 * Adopted from org.exoplatform.onlyoffice.webui.FileUIActivity
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: FileUIActivity.java 00000 Feb 20, 2019 pnedonosko $
 */
@ComponentConfigs({
    @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "classpath:groovy/ecm/social-integration/plugin/space/FileUIActivity.gtmpl", events = {
        @EventConfig(listeners = FileUIActivity.ViewDocumentActionListener.class),
        @EventConfig(listeners = BaseUIActivity.LoadLikesActionListener.class),
        @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
        @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
        @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
        @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
        @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class),
        @EventConfig(listeners = FileUIActivity.OpenFileActionListener.class),
        @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class),
        @EventConfig(listeners = BaseUIActivity.LikeCommentActionListener.class),
        @EventConfig(listeners = BaseUIActivity.EditActivityActionListener.class),
        @EventConfig(listeners = BaseUIActivity.EditCommentActionListener.class) }), })
public class FileUIActivity extends org.exoplatform.wcm.ext.component.activity.FileUIActivity {

  /** The Constant LOG. */
  private static final Log          LOG         = ExoLogger.getLogger(FileUIActivity.class);

  /** The editor service. */
  protected final EditorService     editorService;

  /** The editor service. */
  protected final WOPIService       wopiService;

  /** The editor links. */
  protected final Map<Node, String> editorLinks = new ConcurrentHashMap<>();

  /**
   * Instantiates a new file UI activity with Edit Online button for office
   * documents.
   *
   * @throws Exception the exception
   */
  public FileUIActivity() throws Exception {
    super();
    this.editorService = this.getApplicationComponent(EditorService.class);
    this.wopiService = this.getApplicationComponent(WOPIService.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void end() throws Exception {
    String activityId = getActivity().getId();

    WebuiRequestContext webuiContext = WebuiRequestContext.getCurrentInstance();
    JavascriptManager js = webuiContext.getJavascriptManager();
    RequireJS require = js.require("SHARED/officeonline", "officeonline");

    if (getFilesCount() == 1) {
      Node node = getContentNode(0);
      node = editorService.getNode(node.getSession().getWorkspace().getName(), node.getPath());
      if (node != null) {
        require.addScripts("officeonline.initActivity('" + node.getUUID() + "', " + editorLink(node) + ",'" + activityId + "');");
      }
    }

    // Init preview links for each of file
    for (int index = 0; index < getFilesCount(); index++) {
      Node symlink = getContentNode(index);
      Node node = editorService.getNode(symlink.getSession().getWorkspace().getName(), symlink.getPath());
      if (node != null) {
        require.addScripts("officeonline.initPreview('" + node.getUUID() + "', " + editorLink(node) + " ,'"
            + new StringBuilder("#Preview").append(activityId).append('-').append(index).toString() + "');");
      }
    }
    super.end();
  }

  /**
   * Context editor link.
   *
   * @param node the node
   * @param context the context
   * @return the string
   */
  private String editorLink(Node node) {
    String link = editorLinks.computeIfAbsent(node, n -> wopiService.getEditorLink(node));
    if (link == null || link.isEmpty()) {
      return "null".intern();
    } else {
      return new StringBuilder().append('\'').append(link).append('\'').toString();
    }
  }

}
