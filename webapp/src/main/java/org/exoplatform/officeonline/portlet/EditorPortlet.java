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
package org.exoplatform.officeonline.portlet;

import static org.exoplatform.officeonline.webui.OfficeOnlineContext.callModule;
import static org.exoplatform.officeonline.webui.OfficeOnlineContext.showError;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.jcr.RepositoryException;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.officeonline.Config;
import org.exoplatform.officeonline.OfficeOnlineEditorException;
import org.exoplatform.officeonline.OfficeOnlineEditorService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

/**
 * The Class EditorPortlet.
 */
public class EditorPortlet extends GenericPortlet {

  /** The Constant LOG. */
  private static final Log        LOG = ExoLogger.getLogger(EditorPortlet.class);

  /** The officeonline. */
  private OfficeOnlineEditorService officeonline;

  /** The i 18 n service. */
  private ResourceBundleService   i18nService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() throws PortletException {
    super.init();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    this.officeonline = container.getComponentInstanceOfType(OfficeOnlineEditorService.class);
    this.i18nService = container.getComponentInstanceOfType(ResourceBundleService.class);
  }

  /**
   * Renderer the portlet view.
   *
   * @param request the request
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws PortletException the portlet exception
   */
  @RenderMode(name = "view")
  public void view(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    WebuiRequestContext webuiContext = WebuiRequestContext.getCurrentInstance();
    // ResourceBundle i18n = webuiContext.getApplicationResourceBundle();
    ResourceBundle i18n = i18nService.getResourceBundle(
                                                        new String[] { "locale.officeonline.OfficeOnline",
                                                            "locale.officeonline.OfficeOnlineClient" },
                                                        request.getLocale());

    String docId = webuiContext.getRequestParameter("docId");
    if (docId != null) {
      try {
        Config config = officeonline.createEditor(request.getScheme(),
                                                request.getServerName(),
                                                request.getServerPort(),
                                                request.getRemoteUser(),
                                                null,
                                                docId);
        if (config != null) {
          if (config.getEditorConfig().getLang() == null) {
            if (request.getLocale() != null) {
              // If user lang not defined use current request one
              config.getEditorConfig().setLang(request.getLocale().getLanguage());
            } else {
              // Otherwise use system default one
              config.getEditorConfig().setLang(Locale.getDefault().getLanguage());
            }
          }
          callModule("initEditor(" + config.toJSON() + ");");
        } else {
          showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                    i18n.getString("OfficeOnlineEditor.error.EditorCannotBeCreated"));
        }
      } catch (RepositoryException e) {
        LOG.error("Error reading document node by ID: {}", docId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeOnlineEditor.error.CannotReadDocument"));
      } catch (OfficeOnlineEditorException e) {
        LOG.error("Error creating document editor for node by ID: {}", docId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeOnlineEditor.error.CannotCreateEditor"));
      } catch (JsonException e) {
        LOG.error("Error converting editor configuration to JSON for node by ID: {}", docId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeOnlineEditor.error.CannotSendEditorConfiguration"));
      } catch (Exception e) {
        LOG.error("Error initializing editor for node by ID: {}", docId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeOnlineEditor.error.CannotSendEditorConfiguration"));
      }
    } else {
      showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"), i18n.getString("OfficeonlineEditor.error.DocumentIdRequired"));
    }

    PortletRequestDispatcher prDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/pages/editor.jsp");
    prDispatcher.include(request, response);
  }
}
