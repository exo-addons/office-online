/*
 * Copyright (C) 2003-2018 eXo Platform SAS.
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
package org.exoplatform.officeonline.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URI;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.officeonline.DocumentContent;
import org.exoplatform.officeonline.EditorConfig;
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.officeonline.exception.AuthenticationFailedException;
import org.exoplatform.officeonline.exception.EditorConfigNotFoundException;
import org.exoplatform.officeonline.exception.FileExtensionNotFoundException;
import org.exoplatform.officeonline.exception.FileLockedException;
import org.exoplatform.officeonline.exception.FileNotFoundException;
import org.exoplatform.officeonline.exception.IllegalFileNameException;
import org.exoplatform.officeonline.exception.InvalidFileNameException;
import org.exoplatform.officeonline.exception.LockMismatchException;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.officeonline.exception.PermissionDeniedException;
import org.exoplatform.officeonline.exception.ProofKeyValidationException;
import org.exoplatform.officeonline.exception.SizeMismatchException;
import org.exoplatform.officeonline.exception.UpdateConflictException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

// TODO: Auto-generated Javadoc
/**
 * The Class WOPIService.
 */
@Path("/wopi")
public class WOPIResource implements ResourceContainer {

  /** The Constant ACCESS_TOKEN. */
  public static final String ACCESS_TOKEN            = "access_token";

  /** The Constant WRONG_TOKEN. */
  public static final String WRONG_TOKEN_ATTRIBUTE   = "wrong_token";

  /** The Constant ACCESS_TOKEN. */
  public static final String EDITOR_CONFIG_ATTRIBUTE = "editorConfig";

  /**
   * The Enum Operation.
   */
  public enum Operation {

    /** The get lock. */
    GET_LOCK,
    /** The get share url. */
    GET_SHARE_URL,
    /** The lock. */
    LOCK,
    /** The put. */
    PUT,
    /** The put relative. */
    PUT_RELATIVE,
    /** The refresh lock. */
    REFRESH_LOCK,
    /** The rename file. */
    RENAME_FILE,
    /** The unlock. */
    UNLOCK,
    /** The delete. */
    DELETE
  }

  /** The Constant FILE_CONVERSION. */
  protected static final String FILE_CONVERSION           = "X-WOPI-FileConversion";

  /** The Constant ITEM_VERSION. */
  protected static final String ITEM_VERSION              = "X-WOPI-ItemVersion";

  /** The Constant LOCK. */
  protected static final String LOCK                      = "X-WOPI-Lock";

  /** The Constant MAX_EXPECTED_SIZE. */
  protected static final String MAX_EXPECTED_SIZE         = "X-WOPI-MaxExpectedSize";

  /** The Constant OLD_LOCK. */
  protected static final String OLD_LOCK                  = "X-WOPI-OldLock";

  /** The Constant OVERRIDE. */
  protected static final String OVERRIDE                  = "X-WOPI-Override";

  /** The Constant PROOF. */
  protected static final String PROOF                     = "X-WOPI-Proof";

  /** The Constant PROOF_OLD. */
  protected static final String PROOF_OLD                 = "X-WOPI-ProofOld";

  /** The Constant RELATIVE_TARGET. */
  protected static final String RELATIVE_TARGET           = "X-WOPI-RelativeTarget";

  /** The Constant OVERWRITE_RELATIVE_TARGET. */
  protected static final String OVERWRITE_RELATIVE_TARGET = "X-WOPI-OverwriteRelativeTarget";

  /** The Constant REQUESTED_NAME. */
  protected static final String REQUESTED_NAME            = "X-WOPI-RequestedName";

  /** The Constant SUGGESTED_TARGET. */
  protected static final String SUGGESTED_TARGET          = "X-WOPI-SuggestedTarget";

  /** The Constant INVALID_FILE_NAME_ERROR. */
  protected static final String INVALID_FILE_NAME_ERROR   = "X-WOPI-InvalidFileNameError";

  /** The Constant URL_TYPE. */
  protected static final String URL_TYPE                  = "X-WOPI-UrlType";

  /** The Constant TIMESTAMP. */
  protected static final String TIMESTAMP                 = "X-WOPI-TimeStamp";

  /** The Constant API_VERSION. */
  protected static final String API_VERSION               = "1.1";

  /** The Constant LOG. */
  protected static final Log    LOG                       = ExoLogger.getLogger(WOPIService.class);

  /** The editor service. */
  protected final WOPIService   wopiService;

  /**
   * Instantiates a new WOPI service.
   *
   * @param wopiService the wopi service
   */
  public WOPIResource(WOPIService wopiService) {
    this.wopiService = wopiService;
  }

  /**
   * Files.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param context the context
   * @param operation the operation
   * @param fileId the file id
   * @return the response
   */
  @POST
  @Path("/files/{fileId}/contents")
  @Produces(MediaType.APPLICATION_JSON)
  public Response putFile(@Context UriInfo uriInfo,
                          @Context HttpServletRequest request,
                          @Context ServletContext context,
                          @HeaderParam(OVERRIDE) Operation operation,
                          @PathParam("fileId") String fileId) {

    logDebug("WOPI Request handled: putFile");
    try {
      verifyProofKey(request);
    } catch (ProofKeyValidationException e) {
      logDebug("Proof key validation failed for putFile", e);
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    if (operation == Operation.PUT) {
      try {
        EditorConfig config = getEditorConfig(context);
        String lockId = request.getHeader(LOCK);
        wopiService.putFile(config, lockId, request.getInputStream());
        logDebug("PutFile response OK. LockId: " + lockId);
        return Response.status(Status.OK).header(LOCK, lockId).type(MediaType.APPLICATION_JSON).build();
      } catch (FileNotFoundException e) {
        logDebug("File not found for putFile", e);
        return Response.status(Status.NOT_FOUND)
                       .entity("{\"error\": \"" + e.getMessage() + "\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (LockMismatchException | SizeMismatchException e) {
        logDebug("Lock mismatch/size mismatch for putFile. Provided lock: " + request.getHeader(LOCK) + " Actual lock + "
            + e.getLockId());
        return Response.status(Status.CONFLICT)
                       .entity("{\"error\": \"" + e.getMessage() + "\"}")
                       .header(LOCK, e.getLockId())
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (PermissionDeniedException e) {
        LOG.warn("Cannot save document content.", e);
        return Response.status(Status.FORBIDDEN)
                       .entity("{\"error\": \"Permission denied\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (AuthenticationFailedException e) {
        logDebug("Authentication failed for putFile", e);
        return Response.status(Status.UNAUTHORIZED)
                       .entity("{\"error\": \"" + e.getMessage() + "\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (OfficeOnlineException e) {
        logDebug("Error occured while putFile", e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"" + e.getMessage() + "\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (IOException e) {
        logDebug("Cannot get request body for putFile", e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"Cannot get request body\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (Exception e) {
        logDebug("Exception occured while putFile", e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"Internal error while saving content\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      }
    } else {
      logDebug("Wrong operation for putFile");
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"Wrong operation\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * Files.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param context the context
   * @param fileId the file id
   * @return the response
   */
  @GET
  @Path("/files/{fileId}/contents")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFile(@Context UriInfo uriInfo,
                          @Context HttpServletRequest request,
                          @Context ServletContext context,
                          @PathParam("fileId") String fileId) {

    logDebug("WOPI Request handled: getFile");
    try {
      verifyProofKey(request);
    } catch (ProofKeyValidationException e) {
      logDebug("Proof key validation failed for getFile", e);
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    try {
      EditorConfig config = getEditorConfig(context);
      if (!fileId.equals(config.getFileId())) {
        logDebug("Provided fileId doesn't match fileId from access token");
        return Response.status(Status.BAD_REQUEST)
                       .entity("{\"error\": \"Provided fileId doesn't match fileId from access token\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      }

      DocumentContent content = wopiService.getContent(config);
      String version = null;
      try {
        version = content.getVersion();
      } catch (RepositoryException e) {
        LOG.error("Cannot get node version", e);
      }
      logDebug("Get file response: OK");
      return Response.ok()
                     .header(ITEM_VERSION, version != null ? version : "")
                     .entity(content.getData())
                     .type(content.getType())
                     .build();
    } catch (FileNotFoundException e) {
      logDebug("File not found for getFile ", e);
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (AuthenticationFailedException e) {
      logDebug("Authentication Failed for getFile ", e);
      return Response.status(Status.UNAUTHORIZED)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (OfficeOnlineException e) {
      logDebug("Error occured while getFile ", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * Files.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param context the context
   * @param operation the operation
   * @param fileId the file id
   * @return the response
   */
  @POST
  @Path("/files/{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response files(@Context UriInfo uriInfo,
                        @Context HttpServletRequest request,
                        @Context ServletContext context,
                        @HeaderParam(OVERRIDE) Operation operation,
                        @PathParam("fileId") String fileId) {

    try {
      verifyProofKey(request);
    } catch (ProofKeyValidationException e) {
      logDebug("Proof key validation failed for /files/", e);
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    EditorConfig config;
    try {
      config = getEditorConfig(context);
    } catch (AuthenticationFailedException e) {
      logDebug("Authentication failed for /files/", e);
      return Response.status(Status.UNAUTHORIZED)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (EditorConfigNotFoundException e) {
      logDebug("Editor Config not found failed for /files/", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    if (!fileId.equals(config.getFileId())) {
      logDebug("Provided fileId doesn't match fileId from access token for /files/");
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"Provided fileId doesn't match fileId from access token\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    switch (operation) {
    case GET_LOCK:
      return getLock(config);
    case GET_SHARE_URL:
      return getShareUrl();
    case LOCK: {
      if (LOG.isDebugEnabled()) {
        LOG.debug("WOPI Request handled: lock");
      }
      String providedLock = request.getHeader(LOCK);
      String oldLock = request.getHeader(OLD_LOCK);
      return lockOrUnlockAndRelock(config, providedLock, oldLock);
    }
    case PUT_RELATIVE: {
      if (LOG.isDebugEnabled()) {
        LOG.debug("WOPI Request handled: putRelative");
      }
      if (request.getHeader(RELATIVE_TARGET) != null && request.getHeader(SUGGESTED_TARGET) != null) {
        logDebug("Headers RELATIVE_TARGET and SUGGESTED_TARGET are mutually exclusive for putRelative");
        return Response.status(Status.BAD_REQUEST)
                       .entity("{\"error\": \"Headers RELATIVE_TARGET and SUGGESTED_TARGET are mutually exclusive.\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      }

      try {
        if (request.getHeader(RELATIVE_TARGET) != null) {
          boolean overwrite = Boolean.parseBoolean(request.getHeader(OVERWRITE_RELATIVE_TARGET));
          return putRelativeFile(config, request.getHeader(RELATIVE_TARGET), overwrite, request.getInputStream());
        }
        if (request.getHeader(SUGGESTED_TARGET) != null) {
          return putSuggestedFile(config, request.getHeader(SUGGESTED_TARGET), request.getInputStream());
        }
      } catch (IOException e) {
        logDebug("Cannot get request body for putRelative", e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"Cannot get request body\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      }
    }
    case REFRESH_LOCK: {
      if (LOG.isDebugEnabled()) {
        LOG.debug("WOPI Request handled: refreshLock");
      }
      String providedLock = request.getHeader(LOCK);
      return refreshLock(config, providedLock);
    }
    case RENAME_FILE:
      if (LOG.isDebugEnabled()) {
        LOG.debug("WOPI Request handled: renameFile");
      }
      String name = request.getHeader(REQUESTED_NAME);
      String lock = request.getHeader(LOCK);
      return renameFile(fileId, config, name, lock);
    case UNLOCK: {
      if (LOG.isDebugEnabled()) {
        LOG.debug("WOPI Request handled: unlock");
      }
      String providedLock = request.getHeader(LOCK);
      return unlock(config, providedLock);
    }
    case DELETE: {
      if (LOG.isDebugEnabled()) {
        LOG.debug("WOPI Request handled: delete");
      }
      return delete(config);
    }
    default:
      return Response.status(Status.BAD_REQUEST).build();
    }
  }

  /**
   * Check file info.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param context the context
   * @return the response
   */
  @GET
  @Path("/files/{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response checkFileInfo(@Context UriInfo uriInfo, @Context HttpServletRequest request, @Context ServletContext context) {

    logDebug("WOPI Request handled: checkFileInfo");
    try {
      verifyProofKey(request);
    } catch (ProofKeyValidationException e) {
      logDebug("Proof key validation failed for checkFileInfo", e);
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    URI requestUri = uriInfo.getRequestUri();
    try {
      EditorConfig config = getEditorConfig(context);
      Map<String, Serializable> fileInfo = wopiService.checkFileInfo(requestUri.getScheme(),
                                                                     requestUri.getHost(),
                                                                     requestUri.getPort(),
                                                                     config);
      logDebug("Check file info response: OK");
      return Response.ok(fileInfo).type(MediaType.APPLICATION_JSON).build();
    } catch (AuthenticationFailedException e) {
      logDebug("Authentication failed for checkFileInfo", e);
      return Response.status(Status.UNAUTHORIZED)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (OfficeOnlineException e) {
      logDebug("Error while checkFileInfo", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * Rename file.
   *
   * @param fileId the file id
   * @param config the config
   * @param name the name
   * @param lock the lock
   * @return the response
   */
  private Response renameFile(String fileId, EditorConfig config, String name, String lock) {
    if (!fileId.equals(config.getFileId())) {
      logDebug("Provided fileId doesn't match fileId from access token for renameFile");
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"Provided fileId doesn't match fileId from access token\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    try {
      String title = wopiService.renameFile(config, name, lock);
      logDebug("Rename file response: OK");
      return Response.ok().entity("{\"Name\": \"" + title + "\"}").type(MediaType.APPLICATION_JSON).build();
    } catch (FileNotFoundException e) {
      logDebug("File not found for renameFile", e);
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (PermissionDeniedException e) {
      LOG.warn("Cannot rename file.", e);
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (LockMismatchException e) {
      logDebug("Lock mismatch for renameFile. Provided lock: " + lock + " Actual lock: " + e.getLockId());
      return Response.status(Status.CONFLICT)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .header(LOCK, e.getLockId())
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (InvalidFileNameException e) {
      logDebug("Invalid filename for renameFile", e);
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .header(INVALID_FILE_NAME_ERROR, e.getMessage())
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (OfficeOnlineException | RepositoryException e) {
      LOG.error("Cannot rename file", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"Cannot rename file\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

  }

  /**
   * Lock or unlock and relock.
   *
   * @param config the config
   * @param providedLock the provided lock
   * @param oldLock the old lock
   * @return the response
   */
  private Response lockOrUnlockAndRelock(EditorConfig config, String providedLock, String oldLock) {
    try {
      if (oldLock != null) {
        wopiService.relock(config, providedLock, oldLock);
      } else {
        wopiService.lock(config, providedLock);
      }
      logDebug("Lock or relock response: OK");
      return Response.ok().build();
    } catch (FileNotFoundException e) {
      logDebug("File not found for lock or relock", e);
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (LockMismatchException e) {
      logDebug("Lock mismatch for lock or relock. Provided lock: " + providedLock + " Old LockL " + oldLock + " Actual Lock: " + e.getLockId());
      return Response.status(Status.CONFLICT)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .header(LOCK, e.getLockId())
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (RepositoryException e) {
      LOG.error("Cannot lock or relock file.", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"Cannot lock or relock file.\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

  }

  /**
   * Unlock.
   *
   * @param config the config
   * @param providedLock the provided lock
   * @return the response
   */
  private Response unlock(EditorConfig config, String providedLock) {
    try {
      wopiService.unlock(config, providedLock);
      logDebug("Unlock response: OK");
      return Response.ok().build();
    } catch (FileNotFoundException e) {
      logDebug("File not found for unlock", e);
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (LockMismatchException e) {
      logDebug("Lock mismatch for unlock. Given lock: " + providedLock + ", Actual: " + e.getLockId());
      return Response.status(Status.CONFLICT)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .header(LOCK, e.getLockId())
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (RepositoryException e) {
      LOG.error("Cannot unlock file.", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"Cannot unlock file.\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * Gets the lock.
   *
   * @param config the config
   * @return the response
   */
  private Response getLock(EditorConfig config) {
    try {
      String lock = wopiService.getLockId(config);
      logDebug("GetLock response: OK. Lock: " + lock);
      return Response.ok().header(LOCK, lock != null ? lock : "").build();
    } catch (FileNotFoundException e) {
      logDebug("File not found for getLock", e);
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (RepositoryException e) {
      LOG.error("Cannot get lock of file.", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"Cannot get lock of file\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * Refresh lock.
   *
   * @param config the config
   * @param lockId the lock id
   * @return the response
   */
  private Response refreshLock(EditorConfig config, String lockId) {
    try {
      wopiService.refreshLock(config, lockId);
      logDebug("Refresh lock response: OK");
      return Response.ok().build();
    } catch (FileNotFoundException e) {
      logDebug("File not found for refresh lock", e);
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (LockMismatchException e) {
      logDebug("Lock mismatch for getlock. Given lock: " + lockId + ", Actual: " + e.getLockId());
      return Response.status(Status.CONFLICT)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .header(LOCK, e.getLockId())
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (RepositoryException e) {
      LOG.error("Cannot refresh lock.", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"Cannot refresh lock.\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  private Response delete(EditorConfig config) {
    LOG.warn("WOPI DELETE is not allowed for Office Online for web");
    return Response.ok().build();
  }

  /**
   * Put relative file.
   *
   * @param config the config
   * @param target the target
   * @param overwrite the overwrite
   * @param data the data
   * @return the response
   */
  private Response putRelativeFile(EditorConfig config, String target, boolean overwrite, InputStream data) {
    try {
      wopiService.putRelativeFile(config, target, overwrite, data);
      logDebug("PutRelativeFile response: OK");
      return Response.ok().build();
    } catch (IllegalFileNameException e) {
      logDebug("Illegal file name for PutRelativeFile", e);
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileLockedException e) {
      logDebug("File locked error for PutRelativeFile", e);
      return Response.status(Status.CONFLICT)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .header(LOCK, e.getLockId() != null ? e.getLockId() : "")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (UpdateConflictException e) {
      logDebug("Update conflict for PutRelativeFile", e);
      return Response.status(Status.CONFLICT)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileNotFoundException e) {
      logDebug("File not found for PutRelativeFile", e);
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileExtensionNotFoundException e) {
      logDebug("File extension not found for PutRelativeFile", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (PermissionDeniedException e) {
      LOG.warn("Cannot create new file based on existing one in specific mode.", e);
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (RepositoryException e) {
      LOG.error("Cannot create new file based on existing one in specific mode.", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"Cannot create new file based on existing one in specific mode.\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * Put relative file in suggested mode.
   *
   * @param config the config
   * @param target the target
   * @param data the data
   * @return the response
   */
  private Response putSuggestedFile(EditorConfig config, String target, InputStream data) {
    try {
      wopiService.putSuggestedFile(config, target, data);
      logDebug("PutRelativeFile [Suggested] response: OK");
      return Response.ok().build();
    } catch (FileNotFoundException e) {
      logDebug("File not found for PutRelativeFile [Suggested]");
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileExtensionNotFoundException e) {
      logDebug("File extension not found for PutRelativeFile [Suggested]");
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (PermissionDeniedException e) {
      LOG.warn("Cannot create new file based on existing one in suggested mode.", e);
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (RepositoryException e) {
      LOG.error("Cannot create new file based on existing one in suggested mode.", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"Cannot create new file based on existing one in suggested mode.\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * Gets the share url.
   *
   * @return the share url
   */
  private Response getShareUrl() {
    // TODO get share url
    return null;
  }

  /**
   * Verify proof key.
   *
   * @param request the request
   */
  protected void verifyProofKey(HttpServletRequest request) throws ProofKeyValidationException {
    String proofKeyHeader = request.getHeader(PROOF);
    String oldProofKeyHeader = request.getHeader(PROOF_OLD);
    String timestampHeader = request.getHeader(TIMESTAMP);
    String accessToken = request.getParameter(ACCESS_TOKEN);
    String url = request.getRequestURL().append('?').append(request.getQueryString()).toString().toUpperCase();
    if (!wopiService.verifyProofKey(proofKeyHeader, oldProofKeyHeader, url, accessToken, timestampHeader)) {
      throw new ProofKeyValidationException("Proof key verification failed");
    }
  }

  /**
   * Gets the editor config.
   *
   * @param context the context
   * @return the editor config
   * @throws AuthenticationFailedException the authentication failed exception
   * @throws EditorConfigNotFoundException the editor config not found exception
   */
  protected EditorConfig getEditorConfig(ServletContext context) throws AuthenticationFailedException,
                                                                 EditorConfigNotFoundException {
    EditorConfig config = (EditorConfig) context.getAttribute(EDITOR_CONFIG_ATTRIBUTE);
    if (config != null) {
      return config;
    } else {
      Boolean authFailed = (Boolean) context.getAttribute(WRONG_TOKEN_ATTRIBUTE);
      if (authFailed != null && authFailed.booleanValue()) {
        throw new AuthenticationFailedException("Access token authentication failed");
      } else {
        throw new EditorConfigNotFoundException("Cannot get editor config from context");
      }
    }
  }

  /**
   * Return Office Online REST API version.
   *
   * @param uriInfo - request with base URI
   * @param request the request
   * @return response with
   */
  @GET
  @Path("/api/version")
  @Produces(MediaType.APPLICATION_JSON)
  public Response versionGet(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
    String title = this.getClass().getPackage().getImplementationTitle();
    String version = this.getClass().getPackage().getImplementationVersion();

    String clientHost = getClientHost(request);
    String clientIp = getClientIpAddr(request);
    return Response.ok()
                   .entity("{\"user\": \"" + request.getRemoteUser() + "\",\n\"requestIP\": \"" + clientIp
                       + "\",\n\"requestHost\": \"" + clientHost + "\",\n\"product\":{ \"name\": \"" + title
                       + "\",\n\"version\": \"" + version + "\"},\n\"version\": \"" + API_VERSION + "\"}")
                   .type(MediaType.APPLICATION_JSON)
                   .build();
  }

  /**
   * Gets the client ip addr.
   *
   * @param request the request
   * @return the client ip addr
   */
  protected String getClientIpAddr(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (isValidHost(ip)) {
      // In case of several proxy: X-Forwarded-For: client, proxy1, proxy2
      int commaIdx = ip.indexOf(',');
      if (commaIdx > 0 && commaIdx < ip.length() - 1) {
        // use only client IP
        ip = ip.substring(0, commaIdx);
      }
      return ip;
    }
    ip = request.getHeader("X-Real-IP");
    if (isValidHost(ip)) {
      return ip;
    }
    ip = request.getHeader("Proxy-Client-IP");
    if (isValidHost(ip)) {
      return ip;
    }
    ip = request.getHeader("WL-Proxy-Client-IP");
    if (isValidHost(ip)) {
      return ip;
    }
    ip = request.getHeader("HTTP_CLIENT_IP");
    if (isValidHost(ip)) {
      return ip;
    }
    ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    if (isValidHost(ip)) {
      return ip;
    }
    ip = request.getHeader("HTTP_X_FORWARDED");
    if (isValidHost(ip)) {
      return ip;
    }
    ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
    if (isValidHost(ip)) {
      return ip;
    }
    // http://stackoverflow.com/questions/1634782/what-is-the-most-accurate-way-to-retrieve-a-users-correct-ip-address-in-php
    ip = request.getHeader("HTTP_FORWARDED_FOR");
    if (isValidHost(ip)) {
      return ip;
    }
    ip = request.getHeader("HTTP_FORWARDED");
    if (isValidHost(ip)) {
      return ip;
    }
    ip = request.getHeader("REMOTE_ADDR");
    if (isValidHost(ip)) {
      return ip;
    }
    // last chance to get it from Servlet request
    ip = request.getRemoteAddr();
    if (isValidHost(ip)) {
      return ip;
    }
    return null;
  }

  /**
   * Gets the client host.
   *
   * @param request the request
   * @return the client host
   */
  protected String getClientHost(HttpServletRequest request) {
    String host = request.getHeader("X-Forwarded-Host");
    if (isValidHost(host)) {
      // This header contain requested (!) host name, not a client one, but in
      // case of multi-layer infra
      // (several proxy/firewall) where one of proxy hosts stands in front of
      // actual Document Server and set
      // this header, it will do the job.
      return host;
    }
    // Oct 19, 2017: Solution based on X-Forwarded-For proposed in #3 to work
    // correctly behind reverse proxy
    String clientIp = request.getHeader("X-Forwarded-For");
    if (notEmpty(clientIp)) {
      // In case of several proxy: X-Forwarded-For: client, proxy1, proxy2
      int commaIdx = clientIp.indexOf(',');
      if (commaIdx > 0 && commaIdx < clientIp.length() - 1) {
        // use only client IP
        clientIp = clientIp.substring(0, commaIdx);
      }
    } else {
      // And a case of nginx, try X-Real-IP
      clientIp = request.getHeader("X-Real-IP");
    }
    if (notEmpty(clientIp)) {
      try {
        // XXX For this to work, in server.xml, enableLookups="true" and it can
        // be resource consumption call
        // Thus it could be efficient to use the hosts file of the server
        host = InetAddress.getByName(clientIp).getHostName();
        if (notEmpty(host)) { // host here still may be an IP due to security
                              // restriction
          return host;
        }
      } catch (Exception e) {
        LOG.warn("Cannot obtain client hostname by its IP " + clientIp + ": " + e.getMessage());
      }
    }
    host = request.getRemoteHost();
    if (isValidHost(host)) {
      return host;
    }
    return clientIp; // was null - Dec 20, 2017
  }

  /**
   * Check string is not empty.
   *
   * @param str the str
   * @return true, if not empty, false otherwise
   */
  protected boolean notEmpty(String str) {
    return str != null && str.length() > 0;
  }

  /**
   * Checks if is valid host. It's a trivial check for <code>null</code>, non
   * empty string and not "unknown" text.
   *
   * @param host the host name or IP address
   * @return true, if is valid host
   */
  protected boolean isValidHost(String host) {
    if (notEmpty(host) && !"unknown".equalsIgnoreCase(host)) {
      return true;
    }
    return false;
  }

  protected void logDebug(String msg) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(msg);
    }
  }

  protected void logDebug(String msg, Throwable throwable) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(msg, throwable);
    }
  }

}
