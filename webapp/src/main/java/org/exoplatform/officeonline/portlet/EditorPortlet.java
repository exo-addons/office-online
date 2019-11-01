package org.exoplatform.officeonline.portlet;

import static org.exoplatform.Officeonline.webui.OfficeonlineContext.showError;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.jcr.RepositoryException;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.officeonline.AccessToken;
import org.exoplatform.officeonline.EditorConfig;
import org.exoplatform.officeonline.EditorService;
import org.exoplatform.officeonline.RequestInfo;
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.officeonline.exception.ActionNotFoundException;
import org.exoplatform.officeonline.exception.FileExtensionException;
import org.exoplatform.officeonline.exception.FileNotFoundException;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.ws.frameworks.json.impl.JsonException;

/**
 * The Class EditorPortlet.
 */
public class EditorPortlet extends GenericPortlet {

  /** The Constant DEFAULT_ACTION. */
  private static final String   DEFAULT_ACTION = "edit";

  /** The Constant LOG. */
  private static final Log      LOG            = ExoLogger.getLogger(EditorPortlet.class);

  /** The Officeonline. */
  private EditorService         editorService;

  /** The Officeonline. */
  private WOPIService           wopiService;

  /** The i 18 n service. */
  private ResourceBundleService i18nService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() throws PortletException {
    super.init();
    ExoContainer container = PortalContainer.getInstance();
    this.editorService = container.getComponentInstanceOfType(EditorService.class);
    this.wopiService = container.getComponentInstanceOfType(WOPIService.class);
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

    ResourceBundle i18n = i18nService.getResourceBundle(new String[] { "locale.officeonline.OfficeonlineClient" },
                                                        request.getLocale());

    WebuiRequestContext webuiContext = WebuiRequestContext.getCurrentInstance();
    String fileId = webuiContext.getRequestParameter("fileId");
    JavascriptManager js = webuiContext.getJavascriptManager();
    RequireJS require = js.require("SHARED/officeonline", "officeonline");
    if (fileId != null) {
      try {
        EditorConfig config = editorService.createEditorConfig(request.getRemoteUser(), fileId, null);
        AccessToken token = config.getAccessToken();
        RequestInfo requestInfo = new RequestInfo(request.getScheme(),
                                                  request.getServerName(),
                                                  request.getServerPort(),
                                                  request.getRemoteUser());
        String actionURL = wopiService.getActionUrl(requestInfo, fileId, null, DEFAULT_ACTION);
        require.addScripts("officeonline.initEditor(" + token.toJSON() + ", \"" + actionURL + "\");");
      } catch (RepositoryException e) {
        LOG.error("Error reading document node by ID: {}", fileId, e);
        showError(i18n.getString("OfficeonlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeonlineEditor.error.CannotReadDocument"),
                  require);
      } catch (JsonException e) {
        LOG.error("Error creating JSON from access token for node by ID: {}", fileId, e);
        showError(i18n.getString("OfficeonlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeonlineEditor.error.EditorCannotBeCreated"),
                  require);
      } catch (FileNotFoundException e) {
        LOG.error("Error creating editor config. File not found {}", fileId, e);
        showError(i18n.getString("OfficeonlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeonlineEditor.error.FileNotFound"),
                  require);
      } catch (FileExtensionException e) {
        LOG.error("Error while getting file extension. ID: {}", fileId, e);
        showError(i18n.getString("OfficeonlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeonlineEditor.error.WrongExtension"),
                  require);
      } catch (ActionNotFoundException e) {
        LOG.error("Error getting actionURL by fileId and action. FileId: {}", fileId, e);
        showError(i18n.getString("OfficeonlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeonlineEditor.error.ActionNotFound"),
                  require);
      } catch (OfficeOnlineException e) {
        LOG.error("Error creating document editor for node by ID: {}", fileId, e);
        showError(i18n.getString("OfficeonlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeonlineEditor.error.EditorCannotBeCreated"),
                  require);
      }
    } else {
      LOG.error("Error initializing editor configuration for node by ID: {}", fileId);
      showError(i18n.getString("OfficeonlineEditorClient.ErrorTitle"),
                i18n.getString("OfficeonlineEditor.error.DocumentIdRequired"),
                require);
    }

    PortletRequestDispatcher prDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/pages/editor.jsp");
    prDispatcher.include(request, response);
  }

  protected void showError(String title, String message, RequireJS require) {
    require.addScripts(new StringBuilder("officeonline.showError('").append(title).append("', '" + message + "');").toString());
  }

}
