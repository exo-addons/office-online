package org.exoplatform.officeonline.documents;

import javax.jcr.Node;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.services.cms.documents.DocumentTemplate;
import org.exoplatform.services.cms.documents.NewDocumentEditorPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * The Class OnlyOfficeNewDocumentEditorPlugin.
 */
public class OfficeOnlineNewDocumentEditorPlugin extends BaseComponentPlugin implements NewDocumentEditorPlugin {

  /** The Constant PROVIDER_NAME. */
  protected static final String PROVIDER_NAME = "OfficeOnline";

  /** The Constant LOG. */
  protected static final Log    LOG            = ExoLogger.getLogger(OfficeOnlineNewDocumentEditorPlugin.class);

  /** The wopi service. */
  protected final WOPIService   wopiService;

  /**
   * Instantiates a new office online new document editor plugin.
   *
   * @param wopiService the wopi service
   */
  public OfficeOnlineNewDocumentEditorPlugin(WOPIService wopiService) {
    this.wopiService = wopiService;
  }

  /**
   * On document created.
   *
   * @param workspace the workspace
   * @param path the path
   * @throws Exception the exception
   */
  @Override
  public void onDocumentCreated(String workspace, String path) throws Exception {
    Node node = wopiService.getNode(workspace, path);
    String link = wopiService.getEditorLink(node, WOPIService.EDITNEW_ACTION);
    if (link != null) {
      link = "'" + link + "'";
    } else {
      link = "null";
    }
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    JavascriptManager js = requestContext.getJavascriptManager();
    js.require("SHARED/officeonline", "officeonline").addScripts("officeonline.initEditorPage(" + link + ");");
  }

  /**
   * On document create.
   *
   * @param template the template
   * @param parentPath the parent path
   * @param title the title
   */
  @Override
  public void beforeDocumentCreate(DocumentTemplate template, String parentPath, String title) {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    JavascriptManager js = requestContext.getJavascriptManager();
    js.require("SHARED/officeonline", "officeonline").addScripts("officeonline.initNewDocument();");
  }

  @Override
  public String getProviderName() {
    return PROVIDER_NAME;
  }

}
