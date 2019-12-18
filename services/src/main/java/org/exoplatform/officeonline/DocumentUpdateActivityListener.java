package org.exoplatform.officeonline;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.chain.Context;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wcm.ext.component.activity.listener.FileUpdateActivityListener;

/**
 * The listener interface for receiving documentUpdateActivity events.
 * The class that is interested in processing a documentUpdateActivity
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's addDocumentUpdateActivityListener method. When
 * the documentUpdateActivity event occurs, that object's appropriate
 * method is invoked.
 *
 */
public class DocumentUpdateActivityListener extends FileUpdateActivityListener {

  /** The Constant LOG. */
  protected static final Log  LOG = ExoLogger.getLogger(DocumentUpdateActivityListener.class);

  protected final WOPIService wopiService;

  public DocumentUpdateActivityListener() {
    wopiService = (WOPIService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WOPIService.class);
  }

  /**
   * Event handler.
   *
   * @param event the event
   * @throws Exception the exception
   */
  @Override
  public void onEvent(Event<Context, String> event) throws Exception {
    Context context = event.getSource();
    Property currentProperty = (Property) context.get(InvocationContext.CURRENT_ITEM);
    Node node = currentProperty.getParent().getParent();

    boolean isVersionable = node.isNodeType(WOPIService.MIX_VERSIONABLE);
    Node frozen = isVersionable ? wopiService.getFrozen(node) : node;
    if (wopiService.isEditorVersion(frozen)) {
      Calendar versionDate = frozen.getProperty(WOPIService.EXO_LAST_MODIFIED_DATE).getDate();
      String modifier = frozen.getProperty(WOPIService.EXO_LAST_MODIFIER).getString();
      long timeout = System.currentTimeMillis() - versionDate.getTimeInMillis();

      String versioningUser = wopiService.getVersioningUser(frozen);
      // Version accumulation for same user
      if (!modifier.equals(versioningUser) || timeout >= WOPIService.VERSION_TIMEOUT) {
        super.onEvent(event);
      } 
    } else {
      super.onEvent(event);
    }
  }

  /**
   * Checks if a node has comment.
   *
   * @param node the node
   * @return true if the node is commented
   * @throws RepositoryException the repository exception
   */
  protected boolean isCommentedNode(Node node) throws RepositoryException {
    if (node.hasProperty("eoo:commentId")) {
      String commentId = node.getProperty("eoo:commentId").getString();
      return commentId != null && !commentId.isEmpty();
    }
    return false;
  }
}
