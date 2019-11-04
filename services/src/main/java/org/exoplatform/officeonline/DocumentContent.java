package org.exoplatform.officeonline;

import java.io.InputStream;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: DocumentContent.java 00000 Feb 19, 2016 pnedonosko $
 */
public abstract class DocumentContent {

  /**
   * Constructor for inheritance.
   */
  protected DocumentContent() {
  }

  /**
   * Actual document content. 
   * 
   * @return {@link InputStream}
   */
  public abstract InputStream getData();
  
  /**
   * Document MIME type.
   * 
   * @return {@link String}
   */
  public abstract String getType();
  
  /**
   * Gets the version.
   *
   * @return the version
   */
  public abstract String getVersion();
  
}
