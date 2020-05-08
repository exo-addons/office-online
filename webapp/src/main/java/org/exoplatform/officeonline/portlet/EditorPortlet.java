package org.exoplatform.officeonline.portlet;

import static org.exoplatform.officeonline.webui.OfficeOnlineContext.callModule;
import static org.exoplatform.officeonline.webui.OfficeOnlineContext.showError;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.jcr.Node;
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
import org.exoplatform.officeonline.exception.FileExtensionNotFoundException;
import org.exoplatform.officeonline.exception.FileNotFoundException;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

/**
 * The Class EditorPortlet.
 */
public class EditorPortlet extends GenericPortlet {

  /** The Constant LOG. */
  private static final Log      LOG           = ExoLogger.getLogger(EditorPortlet.class);

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

    ResourceBundle i18n = i18nService.getResourceBundle(new String[] { "locale.officeonline.OfficeOnlineClient" },
                                                        request.getLocale());

    WebuiRequestContext webuiContext = WebuiRequestContext.getCurrentInstance();
    String fileId = webuiContext.getRequestParameter("fileId");
    String action = webuiContext.getRequestParameter("action");
    if (fileId != null) {
      try {
        RequestInfo requestInfo = new RequestInfo(request.getScheme(),
                                                  request.getServerName(),
                                                  request.getServerPort(),
                                                  request.getRemoteUser(),
                                                  request.getLocale());
        EditorConfig config = editorService.createEditorConfig(request.getRemoteUser(), fileId, null, requestInfo);
        AccessToken token = config.getAccessToken();

        Node node = wopiService.nodeByUUID(fileId, null);

        if (action == null) {
          action = WOPIService.EDIT_ACTION;
        }
        if (wopiService.isNewDocument(node)) {
          action = WOPIService.EDITNEW_ACTION;
        }

        if (validAction(node, action)) {
          String actionURL = wopiService.getActionUrl(requestInfo, fileId, null, action);
          String filename = node.getName();
          String versionsUrl = wopiService.getExplorerURL(node, config.getBaseUrl()).append("&versions=true").toString();
          String workspace = node.getSession().getWorkspace().getName();
          InitConfig initConfig = new InitConfig(node.getUUID(), workspace, token, actionURL, versionsUrl, filename);
          callModule("officeonline.initEditor(" + initConfig.toJSON() + ");");
        } else {
          showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                    i18n.getString("OfficeOnlineEditor.error.EditorCannotBeCreated"));
        }

      } catch (RepositoryException e) {
        LOG.error("Error reading document node by ID: {}", fileId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeOnlineEditor.error.CannotReadDocument"));
      } catch (JsonException e) {
        LOG.error("Error creating JSON from access token for node by ID: {}", fileId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeOnlineEditor.error.EditorCannotBeCreated"));
      } catch (FileNotFoundException e) {
        LOG.error("Error creating editor config. File not found {}", fileId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"), i18n.getString("OfficeOnlineEditor.error.FileNotFound"));
      } catch (FileExtensionNotFoundException e) {
        LOG.error("Error while getting file extension. ID: {}", fileId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeOnlineEditor.error.WrongExtension"));
      } catch (ActionNotFoundException e) {
        LOG.error("Error getting actionURL by fileId and action. FileId: {}", fileId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeOnlineEditor.error.ActionNotFound"));
      } catch (OfficeOnlineException e) {
        LOG.error("Error creating document editor for node by ID: {}", fileId, e);
        showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                  i18n.getString("OfficeOnlineEditor.error.EditorCannotBeCreated"));
      }
    } else {
      LOG.error("Error initializing editor configuration for node by ID: {}", fileId);
      showError(i18n.getString("OfficeOnlineEditorClient.ErrorTitle"),
                i18n.getString("OfficeOnlineEditor.error.DocumentIdRequired"));
    }

    PortletRequestDispatcher prDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/pages/editor.jsp");
    prDispatcher.include(request, response);
  }

  /**
   * Valid action.
   *
   * @param node the node
   * @param action the action
   * @return true, if successful
   * @throws RepositoryException the repository exception
   */
  protected boolean validAction(Node node, String action) throws RepositoryException {
    if (action.equals(WOPIService.EDIT_ACTION) && wopiService.canEdit(node)) {
      return true;
    }
    if (action.equals(WOPIService.EDITNEW_ACTION) && wopiService.canEdit(node) && wopiService.isNewDocument(node)) {
      return true;
    }
    if (action.equals(WOPIService.VIEW_ACTION) && wopiService.canView(node)) {
      return true;
    }
    return false;
  }

  /**
   * The Class InitConfig.
   */
  public static class InitConfig {

    /** The file id. */
    private final String fileId;

    /** The workspace. */
    private final String workspace;

    /** The access token. */
    private final AccessToken accessToken;

    /** The action URL. */
    private final String actionURL;

    /** The versions URL. */
    private final String versionsURL;

    /** The filename. */
    private final String filename;

    /**
     * Instantiates a new inits the config.
     *
     * @param fileId the file id
     * @param workspace the workspace
     * @param accessToken the access token
     * @param actionURL the action URL
     * @param versionsURL the versions URL
     * @param filename the filename
     */
    public InitConfig(String fileId,
                      String workspace,
                      AccessToken accessToken,
                      String actionURL,
                      String versionsURL,
                      String filename) {
      this.fileId = fileId;
      this.workspace = workspace;
      this.accessToken = accessToken;
      this.actionURL = actionURL;
      this.versionsURL = versionsURL;
      this.filename = filename;
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
     * Gets the workspace.
     *
     * @return the workspace
     */
    public String getWorkspace() {
      return workspace;
    }

    /**
     * Gets the access token.
     *
     * @return the access token
     */
    public AccessToken getAccessToken() {
      return accessToken;
    }

    /**
     * Gets the action URL.
     *
     * @return the action URL
     */
    public String getActionURL() {
      return actionURL;
    }

    /**
     * Gets the versions URL.
     *
     * @return the versions URL
     */
    public String getVersionsURL() {
      return versionsURL;
    }

    /**
     * Gets the filename.
     *
     * @return the filename
     */
    public String getFilename() {
      return filename;
    }

    /**
     * Return this config as JSON string.
     *
     * @return the string
     * @throws JsonException the json exception
     */
    public String toJSON() throws JsonException {
      JsonGeneratorImpl gen = new JsonGeneratorImpl();
      return gen.createJsonObject(this).toString();
    }

  }

}