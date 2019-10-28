package org.exoplatform.officeonline.portlet;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.officeonline.EditorConfig;
import org.exoplatform.officeonline.EditorService;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

public class EditorPortlet extends GenericPortlet {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(EditorPortlet.class);

  /** The onlyoffice. */
  private EditorService    editorService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() throws PortletException {
    super.init();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    this.editorService = container.getComponentInstanceOfType(EditorService.class);
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

    String fileId = webuiContext.getRequestParameter("fileId");
    if (fileId != null) {
      try {
        EditorConfig config = editorService.createEditorConfig(request.getScheme(),
                                                               request.getServerName(),
                                                               request.getServerPort(),
                                                               request.getRemoteUser(),
                                                               null,
                                                               fileId);
        JavascriptManager js = webuiContext.getJavascriptManager();
        RequireJS require = js.require("SHARED/officeonline", "officeonline");
        require.addScripts("officeonline.initEditor(" + config.toJSON() + ");");
      } catch (RepositoryException e) {
        LOG.error("Error reading document node by ID: {}", fileId, e);

      } catch (OfficeOnlineException e) {
        LOG.error("Error creating document editor for node by ID: {}", fileId, e);

      } catch (JsonException e) {
        LOG.error("Error converting editor configuration to JSON for node by ID: {}", fileId, e);

      } catch (Exception e) {
        LOG.error("Error initializing editor configuration for node by ID: {}", fileId, e);

      }
    } else {
      LOG.error("Error initializing editor configuration for node by ID: {}", fileId);
    }

    PortletRequestDispatcher prDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/pages/editor.jsp");
    prDispatcher.include(request, response);
  }

}
