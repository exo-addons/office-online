/*
 * 
 */
package org.exoplatform.officeonline.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.officeonline.OfficeOnlineEditorService;
import org.exoplatform.services.rest.resource.ResourceContainer;

public class EditorService implements ResourceContainer {
  
  /** The editor service. */
  protected OfficeOnlineEditorService officeService;
  
  
  /**
   * Instantiates a new editor service.
   *
   * @param editorService the editor service
   */
  public EditorService(OfficeOnlineEditorService officeService) {
    this.officeService = officeService;
  }
  

  /**
   * Content.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @return the response
   */
  @GET
  @Path("/editor/content/{fileId}/{accessToken}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response content(@Context UriInfo uriInfo, @Context HttpServletRequest request) {
    // TODO: implement
    return null;
  }
}
