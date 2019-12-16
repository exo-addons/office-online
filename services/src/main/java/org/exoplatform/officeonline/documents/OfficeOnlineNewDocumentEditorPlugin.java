package org.exoplatform.officeonline.documents;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.ecm.webui.component.explorer.documents.NewDocumentEditorPlugin;
import org.exoplatform.officeonline.WOPIService;
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

  /**
   * Instantiates a new only office new document editor plugin.
   *
   * @param initParams the init params
   */
  public OfficeOnlineNewDocumentEditorPlugin(InitParams initParams) {
    ValueParam providerParam = initParams.getValueParam(PROVIDER_PARAM);
    if (providerParam != null) {
      this.provider = providerParam.getValue();
    }
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
   * @param node the node
   * @throws Exception the exception
   */
  @Override
  public void onDocumentCreated(Node node) throws Exception {
    WOPIService wopiService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WOPIService.class);
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
   */
  @Override
  public void beforeDocumentCreate() {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    JavascriptManager js = requestContext.getJavascriptManager();
    js.require("SHARED/officeonline", "officeonline").addScripts("officeonline.initNewDocument();");
  }

}
