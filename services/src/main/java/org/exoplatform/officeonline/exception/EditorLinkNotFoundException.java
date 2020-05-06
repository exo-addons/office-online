package org.exoplatform.officeonline.exception;

/**
 * The Class EditorLinkNotFoundException.
 */
public class EditorLinkNotFoundException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058617L;


  /**
   * Instantiates a new editor link not found exception.
   *
   * @param message the message
   */
  public EditorLinkNotFoundException(String message) {
    super(message);
  }


  /**
   * Instantiates a new editor link not found exception.
   *
   * @param cause the cause
   */
  public EditorLinkNotFoundException(Throwable cause) {
    super(cause);
  }


  /**
   * Instantiates a new editor link not found exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public EditorLinkNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
