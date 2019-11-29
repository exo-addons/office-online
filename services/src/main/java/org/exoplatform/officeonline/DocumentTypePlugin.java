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
package org.exoplatform.officeonline;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The DocumentTypePlugin.
 */
public class DocumentTypePlugin extends BaseComponentPlugin {

  /** The Constant LOG. */
  protected static final Log    LOG                          = ExoLogger.getLogger(DocumentTypePlugin.class);

  /** The DOCUMENT_TYPES_CONFIGURATION param *. */
  private static final String   DOCUMENT_TYPES_CONFIGURATION = "document-types-configuration";

  /** The file extensions. */
  protected Map<String, String> fileExtensions               = Collections.emptyMap();

  /**
   * Initializes a DocumentTypePlugin.
   *
   * @param initParams the initParams
   */
  public DocumentTypePlugin(InitParams initParams) {
    ObjectParameter typesParam = initParams.getObjectParam(DOCUMENT_TYPES_CONFIGURATION);
    if (typesParam != null) {
      Object obj = typesParam.getObject();
      if (obj != null && WOPIService.DocumentTypesConfig.class.isAssignableFrom(obj.getClass())) {
        this.fileExtensions = WOPIService.DocumentTypesConfig.class.cast(obj).getFileExtensions();
      } else {
        this.fileExtensions = new HashMap<>();
        LOG.error("The file extensions are not set");
      }
    }
  }

  /**
   * Gets the file extensions.
   *
   * @return the file extensions
   */
  public Map<String, String> getFileExtensions() {
    return fileExtensions;
  }

}
