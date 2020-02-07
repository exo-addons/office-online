package org.exoplatform.officeonline.documents;

import static org.exoplatform.officeonline.webui.OfficeOnlineContext.callModule;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.Node;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.services.cms.documents.DocumentEditorPlugin;
import org.exoplatform.services.cms.documents.DocumentTemplate;
import org.exoplatform.services.cms.documents.model.EditorButton;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.web.application.JavascriptManager;
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

  /** The i18 n service. */
  private ResourceBundleService     i18nService;

  /** The editor links. */
  protected final Map<Node, String> editorLinks   = new ConcurrentHashMap<>();

  /**
   * Instantiates a new office online new document editor plugin.
   *
   * @param wopiService the wopi service
   * @param i18nService the i18n service
   */
  public OfficeOnlineDocumentEditorPlugin(WOPIService wopiService, ResourceBundleService i18nService) {
    this.wopiService = wopiService;
    this.i18nService = i18nService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onDocumentCreated(String workspace, String path) throws Exception {
    Node node = wopiService.getNode(workspace, path);
    String link = wopiService.getEditorLink(node, WOPIService.EDIT_ACTION);
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
   * {@inheritDoc}
   */
  @Override
  public void beforeDocumentCreate(DocumentTemplate template, String parentPath, String title) {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    JavascriptManager js = requestContext.getJavascriptManager();
    js.require("SHARED/officeonline", "officeonline").addScripts("officeonline.initNewDocument();");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getProviderName() {
    return PROVIDER_NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public EditorButton getEditorButton(String uuid, String workspace, String context) throws Exception {
    Node symlink = wopiService.nodeByUUID(uuid, workspace);
    Node node = wopiService.getNode(symlink.getSession().getWorkspace().getName(), symlink.getPath());
    if (symlink.isNodeType("exo:symlink")) {
      String userId = WebuiRequestContext.getCurrentInstance().getRemoteUser();
      wopiService.addFilePreferences(node, userId, symlink.getPath());
    }
    String link = node != null ? editorLink(node) : null;
    if (link != null) {
      ResourceBundle i18n = i18nService.getResourceBundle(new String[] { "locale.officeonline.OfficeOnlineClient" },
                                                          WebuiRequestContext.getCurrentInstance().getLocale());
      String label = i18n.getString("OfficeonlineEditorClient.EditButtonTitle");
      return new EditorButton(link, label, node.getUUID(), PROVIDER_NAME);
    }
    return null;
  }

  /**
   * Returns editor link, adds it to the editorLinks cache.
   *
   * @param node the node
   * @return the string
   */
  protected String editorLink(Node node) {
    String link = editorLinks.computeIfAbsent(node, n -> {
      if (wopiService.canEdit(node)) {
        return wopiService.getEditorLink(node, WOPIService.EDIT_ACTION);
      } else if (wopiService.canView(node)) {
        return wopiService.getEditorLink(node, WOPIService.VIEW_ACTION);
      }
      return null;
    });
    /*
    if (link == null || link.isEmpty()) {
      return "null".intern();
    } else {
      return new StringBuilder().append('\'').append(link).append('\'').toString();
    }*/
    return link;
  }

  @Override
  public void initActivity(String fileId) throws Exception {
    callModule("officeonline.initActivity(\"" + fileId + "\");");
  }

  @Override
  public void initPreview(String fileId) throws Exception {
    callModule("officeonline.initPreview(\"" + fileId + "\");");
  }

}
