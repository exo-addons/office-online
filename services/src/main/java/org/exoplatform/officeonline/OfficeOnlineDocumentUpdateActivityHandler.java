/*
 * Copyright (C) 2003-2020 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.officeonline;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.commons.chain.Context;

import org.exoplatform.services.cms.documents.DocumentUpdateActivityHandler;
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
public class OfficeOnlineDocumentUpdateActivityHandler extends FileUpdateActivityListener
    implements DocumentUpdateActivityHandler {

  /** Office WOPI service. */
  protected final WOPIService wopiService;

  /** The Constant LOG. */
  protected static final Log  LOG = ExoLogger.getLogger(OfficeOnlineDocumentUpdateActivityHandler.class);

  /**
   * Instantiates a new office online document update activity handler.
   *
   * @param wopiService the wopi service
   */
  public OfficeOnlineDocumentUpdateActivityHandler(WOPIService wopiService) {
    this.wopiService = wopiService;
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
      String modifier = node.getProperty(WOPIService.EXO_LAST_MODIFIER).getString();
      long timeout = System.currentTimeMillis() - versionDate.getTimeInMillis();
      String versioningUser = wopiService.getVersioningUser(frozen);
      if (LOG.isDebugEnabled()) {
        LOG.debug("[Comments] Frozen modifier: {}, versioning user: {}", modifier, versioningUser);
      }
      // Version accumulation for same user
      if (!modifier.equals(versioningUser) || timeout >= WOPIService.VERSION_TIMEOUT) {
        super.onEvent(event);
      }
      return true;
    }
    return false;
  }
}
