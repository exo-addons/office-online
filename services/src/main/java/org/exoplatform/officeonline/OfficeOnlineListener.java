package org.exoplatform.officeonline;

/**
 * The listener interface for receiving officeOnline events.
 * The class that is interested in processing a officeOnline
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's addListener method. When
 * the officeOnline event occurs, that object's appropriate
 * method is invoked.
 *
 */
public interface OfficeOnlineListener {
  
  /**
   * On saved.
   *
   * @param config the config
   */
  void onSaved(EditorConfig config);
}
