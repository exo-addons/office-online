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
package org.exoplatform.officeonline;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.officeonline.Config.Editor;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: CloudDriveService.java 00000 Feb 14, 2013 pnedonosko $
 */
public interface OfficeOnlineEditorService {

  /** The editor opened event. */
  static String EDITOR_OPENED_EVENT = "exo.officeonline.editor.opened";
  
  /** The editor closed event. */
  static String EDITOR_CLOSED_EVENT = "exo.officeonline.editor.closed";
  
  /** The editor saved event. */
  static String EDITOR_SAVED_EVENT = "exo.officeonline.editor.saved";
  
  /** The editor version event. */
  static String EDITOR_VERSION_EVENT = "exo.officeonline.editor.version";
  
  /** The editor error event. */
  static String EDITOR_ERROR_EVENT = "exo.officeonline.editor.error";
  
  /**
   * Return existing editor configuration for given user and node. If editor not
   * open for given node or user then <code>null</code> will be returned. If
   * user not valid then OfficeOnlineEditorException will be thrown.
   *
   * @param userId {@link String}
   * @param workspace {@link String}
   * @param path {@link String}
   * @return {@link Config} or <code>null</code>
   * @throws OfficeOnlineEditorException the officeonline editor exception
   * @throws RepositoryException the repository exception
   */
  Config getEditor(String userId, String workspace, String path) throws OfficeOnlineEditorException, RepositoryException;

  /**
   * Return existing editor for given temporal key. If editor or user not found
   * then <code>null</code> will be returned. If user not valid then
   * OfficeOnlineEditorException will be thrown.
   *
   * @param userId the user id
   * @param key the key, see {@link #getEditor(String, String, String)}
   * @return the editor by key
   * @throws OfficeOnlineEditorException the officeonline editor exception
   * @throws RepositoryException the repository exception
   */
  Config getEditorByKey(String userId, String key) throws OfficeOnlineEditorException, RepositoryException;

  /**
   * Create an editor configuration for given user and node.
   *
   * @param userSchema the schema
   * @param userHost the host
   * @param userPost the user post
   * @param userId {@link String}
   * @param workspace {@link String}
   * @param docId {@link String} a document reference in the workspace, see
   *          {@link #initDocument(String, String)}
   * @return {@link Config} instance in case of successful creation or
   *         <code>null</code> if local file type not supported.
   * @throws OfficeOnlineEditorException if editor exception happened
   * @throws RepositoryException if storage exception happened
   */
  Config createEditor(String userSchema,
                      String userHost,
                      int userPost,
                      String userId,
                      String workspace,
                      String docId) throws OfficeOnlineEditorException, RepositoryException;

  /**
   * Update a configuration associated with given editor {@link Config}
   * instance. A {@link Node} from that the config was created will be updated.
   * This operation will close the editor and it will not be usable after that.
   * 
   * @param userId {@link String}
   * @param status {@link DocumentStatus}
   * @throws OfficeOnlineEditorException if editor exception happened
   * @throws RepositoryException if storage exception happened
   */
  void updateDocument(String userId, DocumentStatus status) throws OfficeOnlineEditorException, RepositoryException;

  /**
   * Inits the document and returns an ID for use within editors. Node may be
   * saved by this method if ID generation will be required, in this case it
   * should be allowed to edit the node (not locked and user has write
   * permissions).
   *
   * @param node the node of the document
   * @return the string with document ID for use within editors
   * @throws OfficeOnlineEditorException the officeonline editor exception
   * @throws RepositoryException the repository exception
   */
  String initDocument(Node node) throws OfficeOnlineEditorException, RepositoryException;

  /**
   * Inits the document and returns an ID for use within editors. Node will be
   * saved by this method.
   *
   * @param workspace the workspace
   * @param path the path
   * @return the string
   * @throws OfficeOnlineEditorException the officeonline editor exception
   * @throws RepositoryException the repository exception
   */
  String initDocument(String workspace, String path) throws OfficeOnlineEditorException, RepositoryException;

  /**
   * Gets the editor page URL for opening at Platform server relatively to the
   * current PortalRequest.
   *
   * @param node the node
   * @return the editor link
   * @throws OfficeOnlineEditorException the officeonline editor exception
   * @throws RepositoryException the repository exception
   */
  String getEditorLink(Node node) throws OfficeOnlineEditorException, RepositoryException;

  /**
   * Gets the editor page URL for opening at Platform server.
   *
   * @param schema the schema
   * @param host the host
   * @param port the port
   * @param workspace the workspace
   * @param docId the doc ID
   * @return the editor link
   */
  String getEditorLink(String schema, String host, int port, String workspace, String docId);

  /**
   * Gets the document node by its path and optionally a repository workspace.
   *
   * @param workspace the workspace, can be <code>null</code>, then a default
   *          one will be used
   * @param path the path of a document
   * @return the document or <code>null</code> if nothing found
   * @throws RepositoryException the repository exception
   * @throws BadParameterException the bad parameter exeption
   */
  Node getDocument(String workspace, String path) throws RepositoryException, BadParameterException;

  /**
   * Get file content.
   *
   * @param userId {@link String}
   * @param fileKey {@link String}
   * @return {@link DocumentContent}
   * @throws OfficeOnlineEditorException the officeonline editor exception
   * @throws RepositoryException the repository exception
   */
  DocumentContent getContent(String userId, String fileKey) throws OfficeOnlineEditorException, RepositoryException;

  /**
   * Check does given host can download document content by this service. It's
   * optional feature, configurable and allow only configured Document server by
   * default.
   * 
   * @param hostName {@link String}
   * @return <code>true</code> if client host with given name can download
   *         document content, <code>false</code> otherwise.
   */
  boolean canDownloadBy(String hostName);

  /**
   * Local state of editing document.
   *
   * @param userId {@link String}
   * @param fileKey {@link String}
   * @return {@link ChangeState}
   * @throws OfficeOnlineEditorException the officeonline editor exception
   */
  ChangeState getState(String userId, String fileKey) throws OfficeOnlineEditorException;

  /**
   * Add listener to the service.
   *
   * @param listener the listener
   */
  void addListener(OfficeOnlineEditorListener listener);

  /**
   * Remove listener from the service.
   *
   * @param listener the listener
   */
  void removeListener(OfficeOnlineEditorListener listener);

  /**
   * Adds DocumentTypePlugin to the service to check mimetypes of documents.
   * 
   * @param plugin - the plugin to be added
   */
  void addTypePlugin(ComponentPlugin plugin);

  /**
   * Checks if the node isn't locked and can be edited by the current user.
   *
   * @param node the node
   * @return true, if the current user can edit the node
   * @throws RepositoryException the repository exeption
   */
  boolean canEditDocument(Node node) throws RepositoryException;

  /**
   * Gets the document ID for given node. It will return an ID for use within an
   * editor, otherwise <code>null</code> will be returned.
   *
   * @param node the node
   * @return the document ID or <code>null</code>
   * @throws OfficeOnlineEditorException the officeonline editor exception
   * @throws RepositoryException the repository exception
   * @see #initDocument(String, String)
   * @see #canEditDocument(Node)
   */
  String getDocumentId(Node node) throws OfficeOnlineEditorException, RepositoryException;

  /**
   * Checks if the node has compatible mime-types.
   *
   * @param node the node
   * @return true, if the node mime-types are supported
   * @throws RepositoryException the repository exeption
   */
  boolean isDocumentMimeSupported(Node node) throws RepositoryException;

  /**
   * Gets the document node by its id and optionally a repository workspace.
   *
   * @param workspace the workspace, can be <code>null</code>, then a default
   *          one will be used
   * @param uuid the id of a document
   * @return the document or <code>null</code> if nothing found
   * @throws RepositoryException the repository exception
   */
  Node getDocumentById(String workspace, String uuid) throws RepositoryException;

  /**
   * Creates a new version of a document.
   * 
   * @param userdata the userdata
   * @param contentUrl the contentUrl
   */
  void downloadVersion(Userdata userdata, String contentUrl);

  /**
   * Gets the last modifier userId.
   * 
   * @param key the key
   * @return the editor user
   */
  Editor.User getLastModifier(String key);

  /**
   * Sets the last modifier userId.
   * 
   * @param key the key
   * @param userId the userId
   */
  void setLastModifier(String key, String userId);

  /**
   * Forces saving a document on document server.
   * 
   * @param userdata the userdata
   */
  void forceSave(Userdata userdata);

  /**
   * Gets a user.
   * 
   * @param key the key
   * @param userId the userId
   * @return the user
   */
  Editor.User getUser(String key, String userId);

  /**
   * Validates the JWT token received from the document server.
   * 
   * @param token the token
   * @param key the document key
   * @return true, if the token is correct, false otherwise
   */
  boolean validateToken(String token, String key);

}
