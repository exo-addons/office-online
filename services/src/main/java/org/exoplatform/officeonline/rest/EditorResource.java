/*
 * 
 */
package org.exoplatform.officeonline.rest;

import java.io.FileNotFoundException;

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
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.officeonline.exception.BadParameterException;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.rest.resource.ResourceContainer;

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
   * @return the response
   */
  @GET
  @Path("/content/{fileId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response content(@Context UriInfo uriInfo,
                          @Context HttpServletRequest request,
                          @Context ServletContext context,
                          @PathParam("fileId") String fileId) {
    EditorConfig config = (EditorConfig) context.getAttribute(WOPIResource.EDITOR_CONFIG_PARAM);
    
    if(config == null) {
      return Response.status(Status.BAD_REQUEST)
          .entity("{\"error\": \"Couldn't obtain editor config from access token\"}")
          .type(MediaType.APPLICATION_JSON)
          .build();
    }
    
    try {
      DocumentContent content = editorService.getContent(fileId, config);
      return Response.ok().entity(content.getData()).type(content.getType()).build();
    } 
    catch (OfficeOnlineException e) {
      return Response.status(Status.BAD_REQUEST)
                     .entity("{\"error\": \"" + e.getMessage() + "\"}")
                     .type(MediaType.APPLICATION_JSON)
                     .build();
    }

  }
}
