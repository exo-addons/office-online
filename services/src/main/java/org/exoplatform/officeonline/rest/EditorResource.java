/*
 * 
 */
package org.exoplatform.officeonline.rest;

import java.net.URLDecoder;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * The Class EditorResource.
 */
@Path("/officeonline/editor")
public class EditorResource implements ResourceContainer {

  /** The editor service. */
  protected EditorService editorService;

  /** The wopi service. */
  protected WOPIService   wopiService;

  /**
   * Instantiates a new editor resource.
   *
   * @param editorService the editor service
   * @param wopiService the wopi service
   */
  public EditorResource(EditorService editorService, WOPIService wopiService) {
    this.editorService = editorService;
    this.wopiService = wopiService;
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

  @GET
  @Path("/configuration/version/accumulation")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("administrators")
  public Response isVersionAccumulationEnabled() {
    return Response.ok()
                   .entity("{\"enabled\": " + wopiService.isVersionAccumulationEnabled() + "}")
                   .type(MediaType.APPLICATION_JSON)
                   .build();

  }
  
  @PUT
  @Path("/configuration/version/accumulation")
  @RolesAllowed("administrators")
  public Response setVersionAccumulation(@FormParam("enabled") Boolean enabled) {
    wopiService.setVersionAccumulation(enabled);
    return Response.ok().build();

  }
}
