package org.exoplatform.officeonline.documents;

import javax.jcr.Node;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
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

  /** The Constant PROVIDER_PARAM. */
  protected static final String PROVIDER_PARAM = "provider";

  /** The Constant LOG. */
  protected static final Log    LOG            = ExoLogger.getLogger(OfficeOnlineNewDocumentEditorPlugin.class);

  /** The provider. */
  protected String              provider;

  /** The wopi service. */
  protected final WOPIService   wopiService;

  /**
   * Instantiates a new office online new document editor plugin.
   *
   * @param wopiService the wopi service
   * @param initParams the init params
   */
  public OfficeOnlineNewDocumentEditorPlugin(WOPIService wopiService, InitParams initParams) {
    ValueParam providerParam = initParams.getValueParam(PROVIDER_PARAM);
    if (providerParam != null) {
      this.provider = providerParam.getValue();
    }
    this.wopiService = wopiService;
  }

  /**
   * Gets the provider.
   *
   * @return the provider
   */
  @Override
  public String getProvider() {
    return provider;
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

}
