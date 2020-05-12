package org.exoplatform.officeonline;

import java.io.InputStream;

import javax.jcr.RepositoryException;

/**
 * The Class DocumentContent.
 */
public abstract class DocumentContent {

  /**
   * Constructor for inheritance.
   */
  protected DocumentContent() {
  }

  /**
   * Gets the filename.
   *
   * @return the filename
   */
  public abstract String getFilename();
  
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
   * @throws RepositoryException the repository exception
   */
  public abstract String getVersion() throws RepositoryException ;

}
