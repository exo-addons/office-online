/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.officeonline.webui;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.officeonline.cometd.CometdConfig;
import org.exoplatform.officeonline.cometd.CometdOfficeOnlineService;
import org.exoplatform.officeonline.exception.OfficeOnlineException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: OfficeOnlineClientContext.java 00000 Mar 18, 2019 pnedonosko $
 */
public class OfficeOnlineContext {

  /** The Constant USERID_ATTRIBUTE. */
  public static final String    USERID_ATTRIBUTE             = "OfficeOnlineContext.userId";

  /** The Constant DOCUMENT_WORKSPACE_ATTRIBUTE. */
  public static final String    DOCUMENT_WORKSPACE_ATTRIBUTE = "OfficeOnlineContext.document.workspace";

  /** The Constant DOCUMENT_PATH_ATTRIBUTE. */
  public static final String    DOCUMENT_PATH_ATTRIBUTE      = "OfficeOnlineContext.document.path";

  /** The Constant JAVASCRIPT. */
  protected static final String JAVASCRIPT                   = "OfficeOnlineContext_Javascript".intern();

  /** The Constant CLIENT_RESOURCE_PREFIX. */
  protected static final String CLIENT_RESOURCE_PREFIX       = "OfficeOnlineEditorClient.";

  /** The Constant LOG. */
  protected static final Log    LOG                          = ExoLogger.getLogger(OfficeOnlineContext.class);

  /** The require. */
  private final RequireJS       require;

  /**
   * Instantiates a new officeonline client context.
   *
   * @param requestContext the request context
   * @throws Exception the exception
   */
  private OfficeOnlineContext(WebuiRequestContext requestContext) throws OfficeOnlineException {
    JavascriptManager js = requestContext.getJavascriptManager();
    this.require = js.require("SHARED/officeonline", "officeonline");

    // Basic JS module initialization
    String messagesJson;
    try {
      ResourceBundleService i18nService = requestContext.getApplication()
                                                        .getApplicationServiceContainer()
                                                        .getComponentInstanceOfType(ResourceBundleService.class);
      ResourceBundle res = i18nService.getResourceBundle("locale.officeonline.OfficeOnlineClient", requestContext.getLocale());
      Map<String, String> resMap = new HashMap<String, String>();
      for (Enumeration<String> keys = res.getKeys(); keys.hasMoreElements();) {
        String key = keys.nextElement();
        String bundleKey;
        if (key.startsWith(CLIENT_RESOURCE_PREFIX)) {
          bundleKey = key.substring(CLIENT_RESOURCE_PREFIX.length());
        } else {
          bundleKey = key;
        }
        resMap.put(bundleKey, res.getString(key));
      }
      messagesJson = new JsonGeneratorImpl().createJsonObjectFromMap(resMap).toString();
    } catch (JsonException e) {
      LOG.warn("Cannot serialize messages bundle JSON", e);
      messagesJson = "{}";
    } catch (Exception e) {
      LOG.warn("Cannot build messages bundle", e);
      messagesJson = "{}";
    }

    ConversationState convo = ConversationState.getCurrent();
    if (convo != null && convo.getIdentity() != null) {
      ExoContainer container = requestContext.getApplication().getApplicationServiceContainer();
      CometdOfficeOnlineService cometdService = container.getComponentInstanceOfType(CometdOfficeOnlineService.class);

      String userId = convo.getIdentity().getUserId();

      CometdConfig cometdConf = new CometdConfig(cometdService.getCometdServerPath(),
                                                 cometdService.getUserToken(userId),
                                                 PortalContainer.getCurrentPortalContainerName());
      try {
        callOnModule("init('" + userId + "', " + cometdConf.toJSON() + ", " + messagesJson + ");");
      } catch (JsonException e) {
        LOG.warn("Cannot create JSON from cometd config: {}", e.getMessage());
        throw new OfficeOnlineException("Failed to convert cometd config to JSON", e);
      }
    } else {
      throw new OfficeOnlineException("Authenticated user required");
    }
  }

  /**
   * App require JS.
   *
   * @return the require JS
   */
  private RequireJS appRequireJS() {
    return require;
  }

  /**
   * Call on module.
   *
   * @param code the code
   */
  private void callOnModule(String code) {
    require.addScripts(new StringBuilder("officeonline.").append(code).append("\n").toString());
  }

  /**
   * Show client error.
   *
   * @param title the title
   * @param message the message
   */
  private void showClientError(String title, String message) {
    callOnModule(new StringBuilder("showError('").append(title).append("', '" + message + "');").toString());
  }

  /**
   * Context.
   *
   * @return the officeonline context
   * @throws OfficeOnlineException the exception
   */
  private static OfficeOnlineContext context() throws OfficeOnlineException {
    OfficeOnlineContext context;
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    Object obj = requestContext.getAttribute(JAVASCRIPT);
    if (obj == null || !OfficeOnlineContext.class.isAssignableFrom(obj.getClass())) {
      synchronized (requestContext) {
        obj = requestContext.getAttribute(JAVASCRIPT);
        if (obj == null || !OfficeOnlineContext.class.isAssignableFrom(obj.getClass())) {
          context = new OfficeOnlineContext(requestContext);
          requestContext.setAttribute(JAVASCRIPT, context);
        } else {
          context = OfficeOnlineContext.class.cast(obj);
        }
      }
    } else {
      context = OfficeOnlineContext.class.cast(obj);
    }
    return context;
  }

  /**
   * Inits the context (current user, CometD settings, etc).
   * on Platform app request start.
   *
   * @throws Exception the exception
   */
  public static void init() throws Exception {
    context();
  }

  /**
   * Adds the script to be called on <code>officeonline</code> module. Finally it
   * will appear as <code>officeonline.myMethod(...)</code>, where myMethod(...)
   * it's what given as code parameter.
   *
   * @param code the code of a method to invoke on officeonline module
   * @throws OfficeOnlineException the exception
   */
  public static void callModule(String code) throws OfficeOnlineException {
    context().callOnModule(code);
  }

  /**
   * Return Web UI app's RequireJS instance.
   *
   * @return the require JS
   * @throws OfficeOnlineException the exception
   */
  public static RequireJS requireJS() throws OfficeOnlineException {
    return context().appRequireJS();
  }

  /**
   * Show error message to an user.
   *
   * @param title the title
   * @param message the message
   */
  public static void showError(String title, String message) {
    try {
      context().showClientError(title, message);
    } catch (Exception e) {
      LOG.error("Error initializing context", e);
    }
  }

}
