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
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.Charset;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;

import com.beetstra.jutf7.CharsetProvider;

import org.exoplatform.officeonline.DocumentContent;
import org.exoplatform.officeonline.EditorConfig;
import org.exoplatform.officeonline.RequestInfo;
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
    DELETE,
    /** The put user info */
    PUT_USER_INFO
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

  /** The Constant UTF-7. */
  private static final Charset  UTF_7                     = new CharsetProvider().charsetForName("UTF-7");

  /** The Constant UTF-8. */
  private static final Charset  UTF_8                     = new CharsetProvider().charsetForName("UTF-8");

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

    if (LOG.isDebugEnabled()) {
      LOG.debug("WOPI Request handled: putFile");
    }
    try {
      verifyProofKey(request);
    } catch (ProofKeyValidationException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Proof key validation failed for putFile");
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    if (operation == Operation.PUT) {
      try {
        EditorConfig config = getEditorConfig(context);
        String lockId = request.getHeader(LOCK);
        wopiService.putFile(config, lockId, request.getInputStream());
        if (LOG.isDebugEnabled()) {
          LOG.debug("PutFile response OK. LockId: " + lockId);
        }
        ResponseBuilder response = Response.ok().header(LOCK, lockId);
        addItemVersionHeader(response, config);
        return response.type(MediaType.APPLICATION_JSON).build();
      } catch (FileNotFoundException e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("File not found for putFile", e);
        }
        return Response.status(Status.NOT_FOUND)
                       .entity("{\"error\": \"" + e.getMessage() + "\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (LockMismatchException | SizeMismatchException e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Lock mismatch/size mismatch for putFile. Provided lock: " + request.getHeader(LOCK) + " Actual lock + "
              + e.getLockId());
        }
        return Response.status(Status.CONFLICT)
                       .entity("{\"error\": \"" + e.getMessage() + "\"}")
                       .header(LOCK, e.getLockId())
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (PermissionDeniedException e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Permission denied for putFile", e);
        }
        return Response.status(Status.FORBIDDEN)
                       .entity("{\"error\": \"Permission denied\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (AuthenticationFailedException e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Authentication failed for putFile");
        }
        return Response.status(Status.UNAUTHORIZED)
                       .entity("{\"error\": \"" + e.getMessage() + "\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (OfficeOnlineException e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Office Online exception occured while handling putFile", e);
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"" + e.getMessage() + "\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (IOException e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Cannot get request body for putFile", e);
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"Cannot get request body\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      } catch (Exception e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Exception occured while handling putFile", e);
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"Internal error while saving content\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      }
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Wrong operation for putFile");
      }
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

    if (LOG.isDebugEnabled()) {
      LOG.debug("WOPI Request handled: getFile");
    }
    try {
      verifyProofKey(request);
    } catch (ProofKeyValidationException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Proof key validation failed for getFile");
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    try {
      EditorConfig config = getEditorConfig(context);
      if (!fileId.equals(config.getFileId())) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Provided fileId doesn't match fileId from access token");
        }
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
      if (LOG.isDebugEnabled()) {
        LOG.debug("Get file response: OK");
      }
      return Response.ok()
                     .header(ITEM_VERSION, version != null ? version : "")
                     .entity(content.getData())
                     .type(content.getType())
                     .build();
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for getFile ", e);
      }
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (AuthenticationFailedException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Authentication Failed for getFile");
      }
      return Response.status(Status.UNAUTHORIZED)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (OfficeOnlineException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Error occured while getFile ", e);
      }
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
      if (LOG.isDebugEnabled()) {
        LOG.debug("Proof key validation failed for /files/");
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    EditorConfig config;
    try {
      config = getEditorConfig(context);
    } catch (AuthenticationFailedException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Authentication failed for /files/");
      }
      return Response.status(Status.UNAUTHORIZED)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (EditorConfigNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Editor Config not found failed for /files/", e);
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    if (!fileId.equals(config.getFileId())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Provided fileId doesn't match fileId from access token for /files/");
      }
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"Provided fileId doesn't match fileId from access token\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("WOPI Request: " + operation);
    }
    switch (operation) {
    case GET_LOCK:
      return getLock(config);
    case GET_SHARE_URL:
      return getShareUrl();
    case LOCK: {
      String providedLock = request.getHeader(LOCK);
      String oldLock = request.getHeader(OLD_LOCK);
      return lockOrUnlockAndRelock(config, providedLock, oldLock);
    }
    case PUT_RELATIVE: {
      if (request.getHeader(RELATIVE_TARGET) != null && request.getHeader(SUGGESTED_TARGET) != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Headers RELATIVE_TARGET and SUGGESTED_TARGET are mutually exclusive for putRelative");
        }
        return Response.status(Status.BAD_REQUEST)
                       .entity("{\"error\": \"Headers RELATIVE_TARGET and SUGGESTED_TARGET are mutually exclusive.\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      }

      try {
        RequestInfo requestInfo = new RequestInfo(request.getScheme(),
                                                  request.getServerName(),
                                                  request.getServerPort(),
                                                  request.getRemoteUser());
        if (request.getHeader(RELATIVE_TARGET) != null) {
          boolean overwrite = Boolean.parseBoolean(request.getHeader(OVERWRITE_RELATIVE_TARGET));
          return putRelativeFile(config, request.getHeader(RELATIVE_TARGET), overwrite, request.getInputStream(), requestInfo);
        }
        if (request.getHeader(SUGGESTED_TARGET) != null) {
          return putSuggestedFile(config, request.getHeader(SUGGESTED_TARGET), request.getInputStream(), requestInfo);
        }
      } catch (IOException e) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Cannot get request body for putRelative", e);
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"Cannot get request body\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      }
    }
    case REFRESH_LOCK: {
      String providedLock = request.getHeader(LOCK);
      return refreshLock(config, providedLock);
    }
    case RENAME_FILE: {
      String name = request.getHeader(REQUESTED_NAME);
      String lock = request.getHeader(LOCK);
      return renameFile(fileId, config, name, lock);
    }
    case UNLOCK: {
      String providedLock = request.getHeader(LOCK);
      return unlock(config, providedLock);
    }
    case DELETE: {
      String lock = request.getHeader(LOCK);
      return delete(config, lock);
    }
    case PUT_USER_INFO: {
      try {
        String userInfo = convertStreamToString(request.getInputStream());
        return putUserInfo(config, userInfo);
      } catch (IOException e) {
        LOG.error("Cannot put userinfo. UserId: " + config.getUserId(), e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"Failed to fetch userinfo from request body\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      }
    }
    default:
      return Response.status(Status.BAD_REQUEST).build();
    }
  }

  protected Response putUserInfo(EditorConfig config, String userInfo) {
    wopiService.putUserInfo(config.getUserId(), userInfo);
    if (LOG.isDebugEnabled()) {
      LOG.debug("PutUserInfo response: OK. UserId: " + config.getUserId() + ". UserInfo: " + userInfo);
    }
    return Response.ok().build();
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

    if (LOG.isDebugEnabled()) {
      LOG.debug("WOPI Request handled: checkFileInfo");
    }
    try {
      verifyProofKey(request);
    } catch (ProofKeyValidationException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Proof key validation failed for checkFileInfo");
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    URI requestUri = uriInfo.getRequestUri();
    try {
      EditorConfig config = getEditorConfig(context);
      Map<String, Serializable> fileInfo = wopiService.checkFileInfo(config);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Check file info response: OK");
      }
      return Response.ok(fileInfo).type(MediaType.APPLICATION_JSON).build();
    } catch (AuthenticationFailedException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Authentication failed for checkFileInfo");
      }
      return Response.status(Status.UNAUTHORIZED)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (OfficeOnlineException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Error while checkFileInfo", e);
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (RepositoryException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Cannot check file info", e);
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"Failed to check file info\"}")
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
      if (LOG.isDebugEnabled()) {
        LOG.debug("Provided fileId doesn't match fileId from access token for renameFile");
      }
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"Provided fileId doesn't match fileId from access token\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    try {
      name = new String(name.getBytes(), UTF_7);
      String title = wopiService.renameFile(config, name, lock);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Rename file response: OK");
      }
      return Response.ok().entity("{\"Name\": \"" + title + "\"}").type(MediaType.APPLICATION_JSON).build();
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for renameFile", e);
      }
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (PermissionDeniedException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Permission denied for rename file.", e);
      }
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (LockMismatchException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Lock mismatch for renameFile. Provided lock: " + lock + " Actual lock: " + e.getLockId());
      }
      return Response.status(Status.CONFLICT)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .header(LOCK, e.getLockId())
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (InvalidFileNameException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Invalid filename for renameFile", e);
      }
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
      if (LOG.isDebugEnabled()) {
        LOG.debug("Lock or relock response: OK");
      }
      ResponseBuilder response = Response.ok();
      addItemVersionHeader(response, config);
      return response.build();
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for lock or relock", e);
      }
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (LockMismatchException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Lock mismatch for lock or relock. Provided lock: " + providedLock + " Old LockL " + oldLock + " Actual Lock: "
            + e.getLockId());
      }
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
      if (LOG.isDebugEnabled()) {
        LOG.debug("Unlock response: OK");
      }
      ResponseBuilder response = Response.ok();
      addItemVersionHeader(response, config);
      return response.build();
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for unlock", e);
      }
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (LockMismatchException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Lock mismatch for unlock. Given lock: " + providedLock + ", Actual: " + e.getLockId());
      }
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
      if (LOG.isDebugEnabled()) {
        LOG.debug("GetLock response: OK. Lock: " + lock);
      }
      return Response.ok().header(LOCK, lock != null ? lock : "").build();
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for getLock", e);
      }
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
      if (LOG.isDebugEnabled()) {
        LOG.debug("Refresh lock response: OK");
      }
      return Response.ok().build();
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for refresh lock", e);
      }
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (LockMismatchException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Lock mismatch for getlock. Given lock: " + lockId + ", Actual: " + e.getLockId());
      }
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

  /**
   * Delete.
   *
   * @param config the config
   * @return the response
   */
  private Response delete(EditorConfig config, String lockId) {
    try {
      wopiService.delete(config, lockId);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Delete response: OK");
      }
      return Response.ok().build();
    } catch (LockMismatchException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Lock mismatch for delete. Given lock: " + lockId + ", Actual: " + e.getLockId());
      }
      return Response.status(Status.CONFLICT)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .header(LOCK, e.getLockId())
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for refresh lock", e);
      }
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();

    } catch (OfficeOnlineException | RepositoryException e) {
      LOG.error("Cannot delete file. FileId: " + config.getFileId() + ", lockId: " + lockId, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"error\": \"Cannot delete file.\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * Put relative file.
   *
   * @param config the config
   * @param target the target
   * @param overwrite the overwrite
   * @param data the data
   * @param requestInfo the request info
   * @return the response
   */
  @Produces("application/json; charset=UTF-8")
  private Response putRelativeFile(EditorConfig config,
                                   String target,
                                   boolean overwrite,
                                   InputStream data,
                                   RequestInfo requestInfo) {
    // Current filename and url
    String currentFileName = "";
    try {
      currentFileName = wopiService.getFileName(config.getFileId(), config.getWorkspace());
    } catch (FileNotFoundException | RepositoryException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Cannot get filename for putFile operation", e);
      }
    }
    String currentUrl = new StringBuilder(wopiService.getWOPISrc(requestInfo, config.getFileId())).append("?access_token=")
                                                                                                  .append(config.getAccessToken()
                                                                                                                .getToken())
                                                                                                  .toString();

    try {
      target = new String(target.getBytes(), UTF_7);
      String fileId = wopiService.putRelativeFile(config, target, overwrite, data);
      String fileName = wopiService.getFileName(fileId, config.getWorkspace());
      EditorConfig newConfig = wopiService.createEditorConfig(config.getUserId(), fileId, config.getWorkspace(), requestInfo);
      String url = new StringBuilder(wopiService.getWOPISrc(requestInfo, fileId)).append("?access_token=")
                                                                                 .append(newConfig.getAccessToken().getToken())
                                                                                 .toString();

      if (LOG.isDebugEnabled()) {
        LOG.debug("PutRelativeFile response: OK");
      }
      return Response.ok()
                     .type(MediaType.APPLICATION_JSON_TYPE)
                     .entity("{\"Name\": \"" + fileName + "\", \"Url\": \"" + url + "\"}")
                     .build();
    } catch (IllegalFileNameException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Illegal file name for PutRelativeFile", e);
      }

      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"Name\": \"" + e.getFilename() + "\", \"Url\": \"no-url\", \"error\": \"" + e.getMessage()
                         + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileLockedException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File locked error for PutRelativeFile", e);
      }
      return Response.status(Status.CONFLICT)
                     .entity("{\"Name\": \"" + e.getFileName() + "\", \"Url\": \"" + currentUrl + "\", \"error\": \""
                         + e.getMessage() + "\"}")
                     .header(LOCK, e.getLockId() != null ? e.getLockId() : "")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (UpdateConflictException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Update conflict for PutRelativeFile", e);
      }
      return Response.status(Status.CONFLICT)
                     .entity("{\"Name\": \"" + e.getFileName() + "\", \"Url\": \"" + currentUrl + "\", \"error\": \""
                         + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for PutRelativeFile", e);
      }
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"Name\": \"" + currentFileName + "\", \"Url\": \"" + currentUrl + "\", \"error\": \""
                         + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileExtensionNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File extension not found for PutRelativeFile", e);
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"Name\": \"" + currentFileName + "\", \"Url\": \"" + currentUrl + "\", \"error\": \""
                         + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (PermissionDeniedException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Cannot create new file based on existing one in specific mode.", e);
      }
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"Name\": \"" + currentFileName + "\", \"Url\": \"" + currentUrl + "\", \"error\": \""
                         + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (OfficeOnlineException | RepositoryException e) {
      LOG.error("Cannot create new file based on existing one in specific mode.", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"Name\": \"" + currentFileName + "\", \"Url\": \"" + currentUrl
                         + "\", \"error\": \"Failed to put relative file in specific mode\"}")
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
   * @param requestInfo the request info
   * @return the response
   */
  private Response putSuggestedFile(EditorConfig config, String target, InputStream data, RequestInfo requestInfo) {
    // Current filename and url
    String currentFileName = "";
    try {
      currentFileName = wopiService.getFileName(config.getFileId(), config.getWorkspace());
    } catch (FileNotFoundException | RepositoryException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Cannot get filename for putFile operation", e);
      }
    }
    String currentUrl = new StringBuilder(wopiService.getWOPISrc(requestInfo, config.getFileId())).append("?access_token=")
                                                                                                  .append(config.getAccessToken()
                                                                                                                .getToken())
                                                                                                  .toString();

    try {
      target = new String(target.getBytes(), UTF_7);
      String fileId = wopiService.putSuggestedFile(config, target, data);
      String fileName = wopiService.getFileName(fileId, config.getWorkspace());
      String url = new StringBuilder(wopiService.getWOPISrc(requestInfo, fileId)).append("?access_token=")
                                                                                 .append(config.getAccessToken().getToken())
                                                                                 .toString();
      String editUrl = wopiService.getEditorURL(fileId, config.getWorkspace(), config.getPlatformUrl());
      // TODO: introduce viewUrl
      String viewUrl = editUrl;
      if (LOG.isDebugEnabled()) {
        LOG.debug("PutRelativeFile [Suggested] response: OK");
      }
      return Response.ok()
                     .entity("{\"Name\": \"" + fileName + "\", \"Url\": \"" + url + "\", \"HostEditUrl\": \"" + editUrl
                         + "\", \"HostViewUrl\":\"" + viewUrl + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for PutRelativeFile [Suggested]");
      }
      return Response.status(Status.NOT_FOUND)
                     .entity("{\"Name\": \"" + currentFileName + "\", \"Url\": \"" + currentUrl + "\", \"error\": \""
                         + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (FileExtensionNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File extension not found for PutRelativeFile [Suggested]");
      }
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"Name\": \"" + currentFileName + "\", \"Url\": \"" + currentUrl + "\", \"error\": \""
                         + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (PermissionDeniedException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Permission denied for PutRelativeFile [Suggested]", e);
      }
      return Response.status(Status.FORBIDDEN)
                     .entity("{\"Name\": \"" + currentFileName + "\", \"Url\": \"" + currentUrl + "\", \"error\": \""
                         + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    } catch (RepositoryException e) {
      LOG.error("Cannot create new file based on existing one in suggested mode.", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR)
                     .entity("{\"Name\": \"" + currentFileName + "\", \"Url\": \"" + currentUrl
                         + "\", \"error\": \"Failed to put relative file in suggested mode\"}")
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
   * @throws ProofKeyValidationException the proof key validation exception
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
   * Adds the item version header.
   *
   * @param response the response
   * @param config the config
   */
  protected void addItemVersionHeader(ResponseBuilder response, EditorConfig config) {
    String version = null;
    try {
      version = wopiService.getFileVersion(config.getFileId(), config.getWorkspace());
    } catch (FileNotFoundException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("File not found for getting version");
      }
    } catch (RepositoryException e) {
      LOG.debug("Error occured while getting file version", e);
    }
    if (version != null) {
      response.header(ITEM_VERSION, version);
    }
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

  protected String convertStreamToString(InputStream inputStream) throws IOException {
    StringWriter writer = new StringWriter();
    IOUtils.copy(inputStream, writer, UTF_8);
    return writer.toString();
  }
}
