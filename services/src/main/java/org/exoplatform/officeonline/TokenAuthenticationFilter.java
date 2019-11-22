package org.exoplatform.officeonline;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.officeonline.rest.WOPIResource;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.web.filter.Filter;

/**
 * The Class TokenAuthenticationFilter.
 */
public class TokenAuthenticationFilter extends AbstractFilter implements Filter {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(TokenAuthenticationFilter.class);

  /**
   * Do filter.
   *
   * @param request the request
   * @param response the response
   * @param chain the chain
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    ServletContext context = request.getServletContext();
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    ExoContainer container = getContainer();
    SessionProviderService sessionProviders =
                                            (SessionProviderService) getContainer().getComponentInstanceOfType(SessionProviderService.class);
    try {
      ExoContainerContext.setCurrentContainer(container);
      EditorConfig config = buildConfig(httpRequest);
      if (config != null) {
        ConversationState state = createState(config.getUserId());
        ConversationState.setCurrent(state);
        SessionProvider userProvider = new SessionProvider(state);
        sessionProviders.setSessionProvider(null, userProvider);
        context.setAttribute(WOPIResource.EDITOR_CONFIG_ATTRIBUTE, config);
      } else {
        context.setAttribute(WOPIResource.WRONG_TOKEN_ATTRIBUTE, true);
      }
      chain.doFilter(request, response);
    } finally {
      context.removeAttribute(WOPIResource.EDITOR_CONFIG_ATTRIBUTE);
      context.removeAttribute(WOPIResource.WRONG_TOKEN_ATTRIBUTE);
      try {
        ConversationState.setCurrent(null);
      } catch (Exception e) {
        LOG.warn("An error occured while cleaning the ThreadLocal", e);
      }
      try {
        ExoContainerContext.setCurrentContainer(null);
      } catch (Exception e) {
        LOG.warn("An error occured while cleaning the ThreadLocal", e);
      }
    }
  }

  /**
   * Creates the state.
   *
   * @param userId the user id
   * @return the conversation state
   */
  private ConversationState createState(String userId) {
    Identity userIdentity = userIdentity(userId);
    if (userIdentity != null) {
      ConversationState state = new ConversationState(userIdentity);
      // Keep subject as attribute in ConversationState.
      state.setAttribute(ConversationState.SUBJECT, userIdentity.getSubject());
      return state;
    }
    LOG.warn("User identity not found " + userId + " for setting conversation state");
    return null;
  }

  /**
   * Builds the config.
   *
   * @param httpRequest the http request
   * @return the editor config
   */
  private EditorConfig buildConfig(HttpServletRequest httpRequest) {
    EditorService editorService = (EditorService) getContainer().getComponentInstanceOfType(EditorService.class);
    String token = httpRequest.getParameter(WOPIResource.ACCESS_TOKEN);
    if (token != null) {
      try {
        return editorService.buildEditorConfig(token);
      } catch (OfficeOnlineException e) {
        LOG.warn("Cannot build editor config from access token {}", e.getMessage());
        return null;
      }
    }
    LOG.warn("Cannot build editor config from access token. Access token is empty");
    return null;
  }

  /**
   * Find or create user identity.
   *
   * @param userId the user id
   * @return the identity can be null if not found and cannot be created via
   *         current authenticator
   */
  protected Identity userIdentity(String userId) {
    IdentityRegistry identityRegistry = (IdentityRegistry) getContainer().getComponentInstanceOfType(IdentityRegistry.class);
    Authenticator authenticator = (Authenticator) getContainer().getComponentInstanceOfType(Authenticator.class);
    Identity userIdentity = identityRegistry.getIdentity(userId);
    if (userIdentity == null) {
      // We create user identity by authenticator, but not register it in the
      // registry
      try {
        if (LOG.isDebugEnabled()) {
          LOG.debug("User identity not registered, trying to create it for: " + userId);
        }
        userIdentity = authenticator.createIdentity(userId);
      } catch (Exception e) {
        LOG.warn("Failed to create user identity: " + userId, e);
      }
    }
    return userIdentity;
  }

  /**
   * Destroy.
   */
  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

}
