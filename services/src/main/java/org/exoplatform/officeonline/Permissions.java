package org.exoplatform.officeonline;

/**
 * The Enum Permissions.
 */
public enum Permissions {

  /** The user can write. */
  USER_CAN_WRITE("UserCanWrite", "W"),

  /** The user can rename. */
  USER_CAN_RENAME("UserCanRename", "R"),

  /** The read only. */
  READ_ONLY("ReadOnly", "RO"),

  /** The user can not write relative. */
  USER_CAN_NOT_WRITE_RELATIVE("UserCanNotWriteRelative", "NWR");

  /** The name. */
  private final String name;

  /** The short name. */
  private final String shortName;

  /**
   * Instantiates a new permissions.
   *
   * @param name the name
   * @param shortName the short name
   */
  private Permissions(String name, String shortName) {
    this.name = name;
    this.shortName = shortName;
  }

  /**
   * To string.
   *
   * @return the string
   */
  public String toString() {
    return this.name;
  }

  /**
   * Gets the short name.
   *
   * @return the short name
   */
  public String getShortName() {
    return this.shortName;
  }

  /**
   * From short name.
   *
   * @param shortName the short name
   * @return the permissions
   */
  public static Permissions fromShortName(String shortName) {
    for (Permissions permission : Permissions.values()) {
      if (permission.shortName.equalsIgnoreCase(shortName)) {
        return permission;
      }
    }
    return null;
  }
}
