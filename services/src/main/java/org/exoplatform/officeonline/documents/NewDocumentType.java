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

package org.exoplatform.officeonline.documents;

/**
 * The Class NewDocumentType.
 */
public class NewDocumentType {

  /** The extension. */
  protected String extension;

  /** The label. */
  protected String label;

  /** The mime type. */
  protected String mimeType;

  /**
   * Gets the extension.
   *
   * @return the extension
   */
  public String getExtension() {
    return extension;
  }

  /**
   * Sets the extension.
   *
   * @param path the new extension
   */
  public void setPath(String extension) {
    this.extension = extension;
  }

  /**
   * Gets the label.
   *
   * @return the label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the label.
   *
   * @param label the new label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Gets the mime type.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the mime type.
   *
   * @param mimeType the new mime type
   */
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new StringBuilder(label).append(" (").append(mimeType).append(", ").append(extension).append(")").toString();
  }

}
