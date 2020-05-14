package org.exoplatform.officeonline;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.commons.chain.Context;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cms.documents.DocumentUpdateActivityHandler;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.listener.Event;
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
public class OfficeOnlineDocumentUpdateActivityHandler extends FileUpdateActivityListener implements DocumentUpdateActivityHandler {

  protected final WOPIService wopiService;

  public OfficeOnlineDocumentUpdateActivityHandler() {
    wopiService = (WOPIService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WOPIService.class);
  }

  @Override
  public boolean handleDocumentUpdateEvent(Event<Context, String> event) throws Exception {
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
      return true;
    }
    return false;
  }
}
