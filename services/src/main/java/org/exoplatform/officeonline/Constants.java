package org.exoplatform.officeonline;

/**
 * The Class Constants.
 * TODO: All remaining constants should be moved to appropriate class
 */
@Deprecated
public class Constants {

  /**
   * Instantiates a new constants.
   */
  private Constants() {
    // constants class
  }

  /** The Constant ACCESS_TOKEN_ATTRIBUTE. */
  public static final String ACCESS_TOKEN_ATTRIBUTE              = "accessToken";

  /** The Constant ACCESS_TOKEN_PARAMETER. */
  public static final String ACCESS_TOKEN_PARAMETER              = "access_token";

  /** The Constant ACCESS_TOKEN_TTL_ATTRIBUTE. */
  public static final String ACCESS_TOKEN_TTL_ATTRIBUTE          = "accessTokenTTL";

  /** The Constant ACTION_CONVERT. */
  public static final String ACTION_CONVERT                      = "convert";

  /** The Constant ACTION_EDIT. */
  public static final String ACTION_EDIT                         = "edit";

  /** The Constant ACTION_VIEW. */
  public static final String ACTION_VIEW                         = "view";

  /** The Constant FILES_ENDPOINT_PATH. */
  public static final String FILES_ENDPOINT_PATH                 = "officeonline/wopi/files/";

  /** The Constant FILE_SCHEMA. */
  public static final String FILE_SCHEMA                         = "file";

  /** The Constant FORM_URL. */
  public static final String FORM_URL                            = "formURL";

  /** The Constant JWT_TOKEN_TTL. */
  public static final int    JWT_TOKEN_TTL                       = 60 * 60;                       // 1h

  /** The Constant NOTIFICATION_DOCUMENT_ID_CODEC_NAME. */
  public static final String NOTIFICATION_DOCUMENT_ID_CODEC_NAME = "notificationDocId";

  /** The Constant OPERATION_CHECK_FILE_INFO. */
  public static final String OPERATION_CHECK_FILE_INFO           = "CheckFileInfo";

  /** The Constant OPERATION_GET_FILE. */
  public static final String OPERATION_GET_FILE                  = "GetFile";

  /** The Constant OPERATION_GET_LOCK. */
  public static final String OPERATION_GET_LOCK                  = "GetLock";

  /** The Constant OPERATION_GET_SHARE_URL. */
  public static final String OPERATION_GET_SHARE_URL             = "GetShareUrl";

  /** The Constant OPERATION_LOCK. */
  public static final String OPERATION_LOCK                      = "Lock";

  /** The Constant OPERATION_PUT_FILE. */
  public static final String OPERATION_PUT_FILE                  = "PutFile";

  /** The Constant OPERATION_PUT_RELATIVE_FILE. */
  public static final String OPERATION_PUT_RELATIVE_FILE         = "PutRelativeFile";

  /** The Constant OPERATION_REFRESH_LOCK. */
  public static final String OPERATION_REFRESH_LOCK              = "RefreshLock";

  /** The Constant OPERATION_RENAME_FILE. */
  public static final String OPERATION_RENAME_FILE               = "RenameFile";

  /** The Constant OPERATION_UNLOCK. */
  public static final String OPERATION_UNLOCK                    = "Unlock";

  /** The Constant OPERATION_UNLOCK_AND_RELOCK. */
  public static final String OPERATION_UNLOCK_AND_RELOCK         = "UnlockAndRelock";

  // -------- WOPI locks directory ---------------

  /** The Constant LOCK_DIRECTORY_DOC_ID. */
  public static final String LOCK_DIRECTORY_DOC_ID               = "docId";

  /** The Constant LOCK_DIRECTORY_FILE_ID. */
  public static final String LOCK_DIRECTORY_FILE_ID              = "fileId";

  /** The Constant LOCK_DIRECTORY_LOCK. */
  public static final String LOCK_DIRECTORY_LOCK                 = "lock";

  /** The Constant LOCK_DIRECTORY_NAME. */
  public static final String LOCK_DIRECTORY_NAME                 = "wopiLocks";

  /** The Constant LOCK_DIRECTORY_REPOSITORY. */
  public static final String LOCK_DIRECTORY_REPOSITORY           = "repository";

  /** The Constant LOCK_DIRECTORY_SCHEMA_NAME. */
  public static final String LOCK_DIRECTORY_SCHEMA_NAME          = "wopiLocks";                   // NOSONAR

  /** The Constant LOCK_DIRECTORY_TIMESTAMP. */
  public static final String LOCK_DIRECTORY_TIMESTAMP            = "timestamp";

  /** The Constant LOCK_EXPIRATION_EVENT. */
  public static final String LOCK_EXPIRATION_EVENT               = "wopiLocksExpiration";

  /** The Constant LOCK_TTL. */
  public static final long   LOCK_TTL                            = 30L * 60 * 1000;               // 30 minutes


  /** The Constant NAME. */
  public static final String NAME                                = "Name";

  /** The Constant URL. */
  public static final String URL                                 = "Url";

}
