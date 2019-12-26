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
package org.exoplatform.officeonline.cometd;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.cometd.annotation.ServerAnnotationProcessor;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.eclipse.jetty.util.component.LifeCycle;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;
import org.picocontainer.Startable;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.officeonline.EditorConfig;
import org.exoplatform.officeonline.OfficeOnlineListener;
import org.exoplatform.officeonline.WOPIService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The CometdOfficeOnlineService.
 */
public class CometdOfficeOnlineService implements Startable {

  /** The Constant LOG. */
  private static final Log              LOG                  = ExoLogger.getLogger(CometdOfficeOnlineService.class);

  /** The channel name. */
  public static final String            CHANNEL_NAME         = "/eXo/Application/OfficeOnline/editor/";

  /** The channel name. */
  public static final String            CHANNEL_NAME_PARAMS  = CHANNEL_NAME + "{fileId}";

  /** The document saved event. */
  public static final String            DOCUMENT_SAVED_EVENT = "DOCUMENT_SAVED";

  /** The WOPIService service. */
  protected final WOPIService           wopiService;

  /** The exo bayeux. */
  protected final EXoContinuationBayeux exoBayeux;

  /** The service. */
  protected final CometdService         service;

  /**
   * Instantiates the CometdOnlyofficeService.
   *
   * @param exoBayeux the exoBayeux
   * @param onlyofficeEditorService the onlyoffice editor service
   */
  public CometdOfficeOnlineService(EXoContinuationBayeux exoBayeux, WOPIService wopiService) {
    this.exoBayeux = exoBayeux;
    this.wopiService = wopiService;
    this.service = new CometdService();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
    // instantiate processor after the eXo container start, to let
    // start-dependent logic worked before us
    final AtomicReference<ServerAnnotationProcessor> processor = new AtomicReference<>();
    // need initiate process after Bayeux server starts
    exoBayeux.addLifeCycleListener(new LifeCycle.Listener() {
      @Override
      public void lifeCycleStarted(LifeCycle event) {
        ServerAnnotationProcessor p = new ServerAnnotationProcessor(exoBayeux);
        processor.set(p);
        p.process(service);
      }

      @Override
      public void lifeCycleStopped(LifeCycle event) {
        ServerAnnotationProcessor p = processor.get();
        if (p != null) {
          p.deprocess(service);
        }
      }

      @Override
      public void lifeCycleStarting(LifeCycle event) {
        // Nothing
      }

      @Override
      public void lifeCycleFailure(LifeCycle event, Throwable cause) {
        // Nothing
      }

      @Override
      public void lifeCycleStopping(LifeCycle event) {
        // Nothing
      }
    });

    if (PropertyManager.isDevelopping()) {
      // This listener not required for work, just for info during development
      exoBayeux.addListener(new BayeuxServer.SessionListener() {
        @Override
        public void sessionRemoved(ServerSession session, boolean timedout) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("sessionRemoved: " + session.getId() + " timedout:" + timedout + " channels: "
                + channelsAsString(session.getSubscriptions()));
          }
        }

        @Override
        public void sessionAdded(ServerSession session, ServerMessage message) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("sessionAdded: " + session.getId() + " channels: " + channelsAsString(session.getSubscriptions()));
          }
        }
      });
    }
  }

  /**
   * The CometService is responsible for sending messages to Cometd channels
   * when a document is saved.
   */
  @Service("officeonline")
  public class CometdService {

    /** The bayeux. */
    @Inject
    private BayeuxServer  bayeux;

    /** The local session. */
    @Session
    private LocalSession  localSession;

    /** The server session. */
    @Session
    private ServerSession serverSession;

    /**
     * Post construct.
     */
    @PostConstruct
    public void postConstruct() {
      wopiService.addListener(new OfficeOnlineListener() {

        @Override
        public void onSaved(EditorConfig config) {
          publishSavedEvent(config.getFileId(), config.getUserId());
        }

      });
    }

    /**
     * Publish saved event.
     *
     * @param docId the doc id
     * @param userId the user id
     * @param comment the comment
     */
    protected void publishSavedEvent(String fileId, String userId) {
      ServerChannel channel = bayeux.getChannel(CHANNEL_NAME + fileId);
      if (channel != null) {
        StringBuilder data = new StringBuilder();
        data.append('{');
        data.append("\"type\": \"");
        data.append(DOCUMENT_SAVED_EVENT);
        data.append("\", ");
        data.append("\"fileId\": \"");
        data.append(fileId);
        data.append("\", ");
        data.append("\"userId\": \"");
        data.append(userId);
        data.append("\"");
        data.append('}');
        channel.publish(localSession, data.toString());
      }
    }

  }

  /**
   * Channels as string.
   *
   * @param channels the channels
   * @return the string
   */
  protected String channelsAsString(Set<ServerChannel> channels) {
    return channels.stream().map(c -> c.getId()).collect(Collectors.joining(", "));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    // Nothing
  }

  /**
   * Gets the cometd server path.
   *
   * @return the cometd server path
   */
  public String getCometdServerPath() {
    return new StringBuilder("/").append(exoBayeux.getCometdContextName()).append("/cometd").toString();
  }

  /**
   * Gets the user token.
   *
   * @param userId the userId
   * @return the token
   */
  public String getUserToken(String userId) {
    return exoBayeux.getUserToken(userId);
  }

}
