/*
 * 
 */
package org.exoplatform.officeonline.rest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
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
import org.exoplatform.officeonline.EditorService;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * The Class EditorResource.
 */
@Path("/officeonline/editor")
public class EditorResource implements ResourceContainer {

  /** The editor service. */
  protected EditorService editorService;

  /**
   * Instantiates a new editor service.
   *
   * @param editorService the editor service
   */
  public EditorResource(EditorService editorService) {
    this.editorService = editorService;
  }

  /**
   * Content.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param context the context
   * @param fileId the file id
   * @return the response
   */
  @GET
  @Path("/content/{fileId}")
  public Response content(@Context UriInfo uriInfo,
                          @Context HttpServletRequest request,
                          @Context ServletContext context,
                          @PathParam("fileId") String fileId) {
    EditorConfig config = (EditorConfig) context.getAttribute(WOPIResource.EDITOR_CONFIG_ATTRIBUTE);

    if (config == null) {
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"Couldn't obtain editor config from access token\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
    if (!fileId.equals(config.getFileId())) {
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"Provided fileId doesn't match fileId from access token\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

    try {
      DocumentContent content = editorService.getContent(config);
      return Response.ok()
                     .header("Content-Type", content.getType())
                     .header("Content-disposition", "attachment; filename=" + content.getFilename())
                     .entity(content.getData())
                     .type(content.getType())
                     .build();
    } catch (OfficeOnlineException e) {
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }
  }

  /**
   * WARNING: ONLY FOR TESTING PURPOSES. SHOULD BE REMOVED ON PRODUCTION
   * 
   * Creates the token.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param context the context
   * @param userId the user id
   * @param fileId the file id
   * @return the response
   */
  @GET
  @Path("/test/token/{userId}/{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response createToken(@Context UriInfo uriInfo,
                              @Context HttpServletRequest request,
                              @Context ServletContext context,
                              @PathParam("userId") String userId,
                              @PathParam("fileId") String fileId) {
    try {
      String token = editorService.createToken(userId, fileId);
      return Response.ok().entity("{\"token\": \"" + token + "\"}").type(MediaType.APPLICATION_JSON).build();
    } catch (OfficeOnlineException e) {
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

  }
}
