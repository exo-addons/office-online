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
import org.exoplatform.officeonline.AccessToken;
import org.exoplatform.officeonline.EditorConfig;
import org.exoplatform.officeonline.EditorService;
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

/**
 * The Class EditorPortlet.
 */
public class EditorPortlet extends GenericPortlet {

  /** The Constant DEFAULT_ACTION. */
  private static final String DEFAULT_ACTION = "edit";

  /** The Constant LOG. */
  private static final Log    LOG            = ExoLogger.getLogger(EditorPortlet.class);

  /** The onlyoffice. */
  private EditorService       editorService;

  /** The onlyoffice. */
  private WOPIService         wopiService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() throws PortletException {
    super.init();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    this.editorService = container.getComponentInstanceOfType(EditorService.class);
    this.wopiService = container.getComponentInstanceOfType(WOPIService.class);
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
        AccessToken token = config.getAccessToken();
        // TODO: choose action instead of using DEFAULT_ACTION
        String actionURL = wopiService.getActionUrl(fileId, null, DEFAULT_ACTION);
        JavascriptManager js = webuiContext.getJavascriptManager();
        RequireJS require = js.require("SHARED/officeonline", "officeonline");
        require.addScripts("officeonline.initEditor(" + token + ", " + actionURL + ");");
      } catch (RepositoryException e) {
        LOG.error("Error reading document node by ID: {}", fileId, e);

      } catch (OfficeOnlineException e) {
        LOG.error("Error creating document editor for node by ID: {}", fileId, e);
      }
    } else {
      LOG.error("Error initializing editor configuration for node by ID: {}", fileId);
    }

    PortletRequestDispatcher prDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/pages/editor.jsp");
    prDispatcher.include(request, response);
  }

}
