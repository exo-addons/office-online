/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.officeonline.portlet;

import javax.portlet.GenericPortlet;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class OfficeOnlineStreamPortlet extends GenericPortlet {

  /**
   * Renders the portlet on a page.
   *
   * @param request the request
   * @param response the response
   */
  @Override
  protected void doView(final RenderRequest request, final RenderResponse response) {
    // This code will be executed once per a portlet app, thus first time when
    // navigated to a portal page, all other calls inside the page (ajax
    // calls), will not cause rendering of this portlet, except if this will not
    // be issued explicitly.
  }

}
