package org.exoplatform.officeonline;

/**
 * The Class Constants.
 */
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

  // -------- End WOPI locks directory ---------------

  // -------- CheckFileInfo ---------------

  // -------- Required properties ---------------

  /** The Constant BASE_FILE_NAME. */
  public static final String BASE_FILE_NAME                      = "BaseFileName";

  /** The Constant OWNER_ID. */
  public static final String OWNER_ID                            = "OwnerId";

  /** The Constant SIZE. */
  public static final String SIZE                                = "Size";

  /** The Constant USER_ID. */
  public static final String USER_ID                             = "UserId";

  /** The Constant VERSION. */
  public static final String VERSION                             = "Version";

  // -------- Host capabilities properties ---------------

  /** The Constant SUPPORTS_EXTENDED_LOCK_LENGTH. */
  public static final String SUPPORTS_EXTENDED_LOCK_LENGTH       = "SupportsExtendedLockLength";

  /** The Constant SUPPORTS_GET_LOCK. */
  public static final String SUPPORTS_GET_LOCK                   = "SupportsGetLock";

  /** The Constant SUPPORTS_LOCKS. */
  public static final String SUPPORTS_LOCKS                      = "SupportsLocks";

  /** The Constant SUPPORTS_RENAME. */
  public static final String SUPPORTS_RENAME                     = "SupportsRename";

  /** The Constant SUPPORTS_UPDATE. */
  public static final String SUPPORTS_UPDATE                     = "SupportsUpdate";

  /** The Constant SUPPORTED_SHARE_URL_TYPES. */
  public static final String SUPPORTED_SHARE_URL_TYPES           = "SupportedShareUrlTypes";

  // -------- User metadata properties ---------------

  /** The Constant IS_ANONYMOUS_USER. */
  public static final String IS_ANONYMOUS_USER                   = "IsAnonymousUser";

  /** The Constant LICENSE_CHECK_FOR_EDIT_IS_ENABLED. */
  public static final String LICENSE_CHECK_FOR_EDIT_IS_ENABLED   = "LicenseCheckForEditIsEnabled";

  /** The Constant USER_FRIENDLY_NAME. */
  public static final String USER_FRIENDLY_NAME                  = "UserFriendlyName";

  // -------- User permissions properties ---------------

  /** The Constant READ_ONLY. */
  public static final String READ_ONLY                           = "ReadOnly";

  /** The Constant USER_CAN_RENAME. */
  public static final String USER_CAN_RENAME                     = "UserCanRename";

  /** The Constant USER_CAN_WRITE. */
  public static final String USER_CAN_WRITE                      = "UserCanWrite";

  /** The Constant USER_CAN_NOT_WRITE_RELATIVE. */
  public static final String USER_CAN_NOT_WRITE_RELATIVE         = "UserCanNotWriteRelative";

  // -------- File URL properties ---------------

  /** The Constant CLOSE_URL. */
  public static final String CLOSE_URL                           = "CloseUrl";

  /** The Constant DOWNLOAD_URL. */
  public static final String DOWNLOAD_URL                        = "DownloadUrl";

  /** The Constant FILE_VERSION_URL. */
  public static final String FILE_VERSION_URL                    = "FileVersionUrl";

  /** The Constant HOST_EDIT_URL. */
  public static final String HOST_EDIT_URL                       = "HostEditUrl";

  /** The Constant HOST_VIEW_URL. */
  public static final String HOST_VIEW_URL                       = "HostViewUrl";

  /** The Constant SIGNOUT_URL. */
  public static final String SIGNOUT_URL                         = "SignoutUrl";

  // -------- Breadcrumb properties ---------------

  /** The Constant BREADCRUMB_BRAND_NAME. */
  public static final String BREADCRUMB_BRAND_NAME               = "BreadcrumbBrandName";

  /** The Constant BREADCRUMB_BRAND_URL. */
  public static final String BREADCRUMB_BRAND_URL                = "BreadcrumbBrandUrl";

  /** The Constant BREADCRUMB_FOLDER_NAME. */
  public static final String BREADCRUMB_FOLDER_NAME              = "BreadcrumbFolderName";

  /** The Constant BREADCRUMB_FOLDER_URL. */
  public static final String BREADCRUMB_FOLDER_URL               = "BreadcrumbFolderUrl";

  // -------- End CheckFileInfo ---------------

  // -------- Rename and PutRelativeFile ---------------

  /** The Constant NAME. */
  public static final String NAME                                = "Name";

  /** The Constant URL. */
  public static final String URL                                 = "Url";

  // -------- End Rename and PutRelativeFile ---------------

  // -------- GetShareUrl ---------------

  /** The Constant SHARE_URL. */
  public static final String SHARE_URL                           = "ShareUrl";

  /** The Constant SHARE_URL_READ_ONLY. */
  public static final String SHARE_URL_READ_ONLY                 = "ReadOnly";

  /** The Constant SHARE_URL_READ_WRITE. */
  public static final String SHARE_URL_READ_WRITE                = "ReadWrite";

  // -------- End GetShareUrl ---------------

}
