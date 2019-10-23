package org.exoplatform.officeonline;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;

public class TokenAuthenticationFilter extends AbstractFilter {

  private static final Log LOG = ExoLogger.getLogger(TokenAuthenticationFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    ExoContainer container = getContainer();
    try {
      ExoContainerContext.setCurrentContainer(container);
      ConversationState state = getCurrentState(container, httpRequest);
      ConversationState.setCurrent(state);
      chain.doFilter(request, response);
    } finally {
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
   * Gives the current state
   */
  private ConversationState getCurrentState(ExoContainer container, HttpServletRequest httpRequest) {
    EditorService editorService = (EditorService) container.getComponentInstanceOfType(EditorService.class);
    SessionProviderService sessionProviders =
                                            (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);

    String token = httpRequest.getParameter("access_token");
    if (token != null) {
      EditorConfig config = null;
      try {
        config = editorService.buildEditorConfig(token);
      } catch (OfficeOnlineException e) {
        LOG.error("Cannot build editor config {}", e.getMessage());
        return null;
      }

      Identity userIdentity = userIdentity(config.getUserId());
      if (userIdentity != null) {
        ConversationState state = new ConversationState(userIdentity);
        // Keep subject as attribute in ConversationState.
        state.setAttribute(ConversationState.SUBJECT, userIdentity.getSubject());
        ConversationState.setCurrent(state);
        SessionProvider userProvider = new SessionProvider(state);
        sessionProviders.setSessionProvider(null, userProvider);
        return state;
      }
      LOG.warn("User identity not found " + config.getUserId() + " for setting conversation state");
    }
    LOG.warn("Authentication token is empty");
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

  @Override
  public void destroy() {
    // TODO Auto-generated method stub

  }

}
