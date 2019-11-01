package org.exoplatform.officeonline.exception;

public class FileExtensionException extends OfficeOnlineException {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 8675025886542058618L;

  public FileExtensionException(String message) {
    super(message);
  }

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param cause the cause
   */
  public FileExtensionException(Throwable cause) {
    super(cause);
  }

  /**
   * Instantiates a new bad parameter exception.
   *
   * @param message the message
   * @param cause the cause
   */
  public FileExtensionException(String message, Throwable cause) {
    super(message, cause);
  }

}
