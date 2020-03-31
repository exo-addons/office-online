/**
 * Office Online Editor client.
 */
(function($, cCometD, redux, editorbuttons) {
  "use strict";

  /** For debug logging. */
  var log = function(msg, err) {
    var logPrefix = "[officeonline] ";
    if (typeof console != "undefined" && typeof console.log != "undefined") {
      var isoTime = " -- " + new Date().toISOString();
      var msgLine = msg;
      if (err) {
        msgLine += ". Error: ";
        if (err.name || err.message) {
          if (err.name) {
            msgLine += "[" + err.name + "] ";
          }
          if (err.message) {
            msgLine += err.message;
          }
        } else {
          msgLine += (typeof err === "string" ? err : JSON.stringify(err)
              + (err.toString && typeof err.toString === "function" ? "; " + err.toString() : ""));
        }

        console.log(logPrefix + msgLine + isoTime);
        if (typeof err.stack != "undefined") {
          console.log(err.stack);
        }
      } else {
        if (err !== null && typeof err !== "undefined") {
          msgLine += ". Error: '" + err + "'";
        }
        console.log(logPrefix + msgLine + isoTime);
      }
    }
  };

  var getEditorButton = function(editorLink) {
    var label = message("OfficeonlineEditorClient.EditButtonTitle");
    var iconClass = "uiIconEdit";
    if (editorLink.indexOf("&action=view") > -1) {
      label = message("OfficeonlineEditorClient.ViewButtonTitle");
      iconClass = "uiIconView";
    }
    return "<li class='hidden-tabletL'><a href='" + editorLink + "' target='_blank'>"
        + "<i class='uiIconEcmsOfficeOnlineOpen uiIconEcmsLightGray " + iconClass + "'></i>" + label + "</a></li>";
  };

  var getNoPreviewEditorButton = function(editorLink) {
    var label = message("OfficeonlineEditorClient.EditButtonTitle");
    var iconClass = "uiIconEdit";
    if (editorLink.indexOf("&action=view") > -1) {
      label = message("OfficeonlineEditorClient.ViewButtonTitle");
      iconClass = "uiIconView";
    }
    return "<a class='btn editInOfficeOnline hidden-tabletL' href='#' onclick='javascript:window.open(\"" + editorLink + "\");'>"
        + "<i class='uiIconEcmsOfficeOnlineOpen uiIconEcmsLightGray " + iconClass + "'></i>" + label + "</a>";
  };

  /**
   * Returns the html markup of the refresh banner;
   */
  var getRefreshBanner = function() {
    return "<div class='documentRefreshBanner'><div class='refreshBannerContent'>" + message("OfficeonlineEditorClient.UpdateBannerTitle")
        + "<span class='refreshBannerLink'>" + message("OfficeonlineEditorClient.ReloadButtonTitle") + "</span></div></div>";
  };

  /**
   * Ads the 'Edit Online' button to the JCRExplorer when a document is displayed.
   */
  var addEditorButtonToExplorer = function(editorLink) {
    var $button = $("#UIJCRExplorer #uiActionsBarContainer i.uiIconEcmsOfficeOnlineOpen");
    if (editorLink.indexOf("&action=view") > -1) {
     var buttonHtml = $button[0].outerHTML;
     $button.parent().html(buttonHtml + " " + message("OfficeonlineEditorClient.ViewButtonTitle"));
     $button = $("#UIJCRExplorer #uiActionsBarContainer i.uiIconEcmsOfficeOnlineOpen");     
     $button.addClass("uiIconView");
    } else {
      $button.addClass("uiIconEdit");
    }
    
    $button.closest("li").addClass("hidden-tabletL");
    var $noPreviewContainer = $("#UIJCRExplorer .navigationContainer.noPreview");
    if ($noPreviewContainer.length != 0) {
      var $detailContainer = $noPreviewContainer.find(".detailContainer");
      var $downloadBtn = $detailContainer.find(".uiIconDownload").closest("a.btn");
      if ($downloadBtn.length != 0) {
        $downloadBtn.after(getNoPreviewEditorButton(editorLink));
      } else {
        $detailContainer.append(getNoPreviewEditorButton(editorLink));
      }
    }
  };

  var refreshPDFPreview = function() {
    var $banner = $(".document-preview-content-file #toolbarContainer .documentRefreshBanner");
    if ($banner.length !== 0) {
      $banner.remove();
    }
    setTimeout(function() {
      var $vieverScript = $(".document-preview-content-file script[src$='/viewer.js']")
      var viewerSrc = $vieverScript.attr("src");
      $vieverScript.remove();
      $(".document-preview-content-file").append("<script src='" + viewerSrc + "'></script>");
    }, 250); // XXX we need wait for office preview server generate a new preview
  };

  /**
   * Ads the refresh banner to the PDF document preview.
   */
  var addRefreshBannerPDF = function() {
    var $toolbarContainer = $(".document-preview-content-file #toolbarContainer");
    if ($toolbarContainer.length !== 0 && $toolbarContainer.find(".documentRefreshBanner").length === 0) {
      $toolbarContainer.append(getRefreshBanner());
      $(".documentRefreshBanner .refreshBannerLink").click(function() {
        refreshPDFPreview();
      });
    }
  };
  
  var refreshActivityPreview = function(activityId, $banner) {
    $banner.find(".refreshBannerContent")
        .append("<div class='loading'><i class='uiLoadingIconSmall uiIconEcmsGray'></i></div>");
    var $refreshLink = $banner.find(".refreshBannerLink");
    $refreshLink.addClass("disabled");
    $refreshLink.on('click', function() {
      return false;
    });
    $refreshLink.attr("href", "#");
    var $img = $("#Preview" + activityId + "-0 #MediaContent" + activityId + "-0 img");
    if ($img.length !== 0) {
      var src = $img.attr("src");
      if (src.includes("version=")) {
        src = src.substring(0, src.indexOf("version="));
      }
      var timestamp = new Date().getTime();

      src += "version=oview_" + timestamp;
      src += "&lastModified=" + timestamp;

      $img.on('load', function() {
        $banner.remove();
      });
      $img.attr("src", src);

      // Hide banner when there no preview image
      var $mediaContent = $("#Preview" + activityId + "-0 #MediaContent" + activityId + "-0");
      var observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
          if (mutation.attributeName === "class") {
            var attributeValue = $(mutation.target).prop(mutation.attributeName);
            if (attributeValue.includes("NoPreview")) {
              log("Cannot load preview for activity " + activityId + ". Hiding refresh banner");
              $banner.remove();
            }
          }
        });
      });
      observer.observe($mediaContent[0], {
        attributes : true
      });
    }
  };
  
  var addRefreshBannerActivity = function(activityId) {
    var $previewParent = $("#Preview" + activityId + "-0").parent();
    // If there is no preview
    if ($previewParent.length === 0 || $previewParent.find(".mediaContent.docTypeContent.NoPreview").length !== 0) {
      return;
    }
    // If the activity contains only one preview
    if ($previewParent.find("#Preview" + activityId + "-1").length === 0) {
      if ($previewParent.find(".documentRefreshBanner").length === 0) {
        $previewParent.prepend(getRefreshBanner());
        var $banner = $previewParent.find(".documentRefreshBanner");
        $(".documentRefreshBanner .refreshBannerLink").click(function() {
          refreshActivityPreview(activityId, $banner);
        });
      }
      log("Activity document: " + activityId + " has been updated");
    }
  };

  /**
   * Parse JSON
   */
  var tryParseJson = function(message) {
    var src = message.data ? message.data : (message.error ? message.error : message.failure);
    if (src) {
      try {
        if (typeof src === "string" && (src.startsWith("{") || src.startsWith("["))) {
          return JSON.parse(src);
        }
      } catch (e) {
        log("Error parsing '" + src + "' as JSON: " + e, e);
      }
    }
    return src;
  };

  /**
   * Stuff grabbed from CW's commons.js
   */
  var pageBaseUrl = function(theLocation) {
    if (!theLocation) {
      theLocation = window.location;
    }

    var theHostName = theLocation.hostname;
    if (theLocation.port) {
      theHostName += ":" + theLocation.port;
    }

    return theLocation.protocol + "//" + theHostName;
  };

  var messages = {}; // should be initialized by init()

  var message = function(key) {
    var m = messages[key];
    return m ? m : key;
  };

  var prefixUrl = pageBaseUrl(location);

  /**
   * Editor core class.
   */
  function Editor() {

    // Constants:
    const DOCUMENT_SAVED = "DOCUMENT_SAVED";
    const FILE_RENAME = "File_Rename";
    const FILE_VERSIONS = "UI_FileVersions";
    
    // Editor Window
    var editorWindow;
    // Current user ID
    var currentUserId;
    // CometD transport bus
    var cometd, cometdContext;
    // Subscribed document
    var subscribedDocuments = {};
    // Explorer fileId
    var explorerFileId;
    // The file extension
    var extension;
    // The versions linked
    var versionsLink;

    // Events that are dispatched to redux as actions
    var dispatchableEvents = [ DOCUMENT_SAVED ];

    // Redux store for dispatching document updates inside the app
    var store = redux.createStore(function(state, action) {
      if (dispatchableEvents.includes(action.type)) {
        return action;
      } else if (action.type.startsWith("@@redux/INIT")) {
        // it's OK (at least for initialization)
      } else {
        log("Unknown action type:" + action.type);
      }
      return state;
    });

    /**
     * Subscribes on a document updates using cometd. Dispatches events to the redux store.
     */
    var subscribeDocument = function(fileId) {
      // Use only one channel for one document
      if (subscribedDocuments.fileId) {
        return;
      }
      var subscription = cometd.subscribe("/eXo/Application/OfficeOnline/editor/" + fileId, function(message) {
        // Channel message handler
        var result = tryParseJson(message);
        if (dispatchableEvents.includes(result.type)) {
          store.dispatch(result);
        }
      }, cometdContext, function(subscribeReply) {
        // Subscription status callback
        if (subscribeReply.successful) {
          // The server successfully subscribed this client to the channel.
          log("Document updates subscribed successfully: " + JSON.stringify(subscribeReply));
          subscribedDocuments.fileId = subscription;
        } else {
          var err = subscribeReply.error ? subscribeReply.error : (subscribeReply.failure ? subscribeReply.failure.reason
              : "Undefined");
          log("Document updates subscription failed for " + fileId, err);
        }
      });
    };
    
    /**
     * Unsubscribes document
     */
    var unsubscribeDocument = function(fileId) {
      var subscription = subscribedDocuments.fileId;
      if (subscription) {
        cometd.unsubscribe(subscription, {}, function(unsubscribeReply) {
          if (unsubscribeReply.successful) {
            // The server successfully unsubscribed this client to the channel.
            log("Document updates unsubscribed successfully for: " + fileId);
            delete subscribedDocuments.fileId;
          } else {
            var err = unsubscribeReply.error ? unsubscribeReply.error
                : (unsubscribeReply.failure ? unsubscribeReply.failure.reason : "Undefined");
            log("Document updates unsubscription failed for " + fileId, err);
          }
        });
      }
    };
    
    /**
     * Updates window title
     */
    var updateWindowTitle = function(filename) {
      if(!filename.endsWith(extension)) {
        filename += extension;
      }
      window.document.title = filename + " - " + message("OfficeonlineEditorClient.EditorTitle");
    };
    
    /**
     * Handles PostMessages from Office Online frame
     */
    var handlePostMessage = function(e) {
      var msg = JSON.parse(e.data);
      // Rename
      if(msg.MessageId == FILE_RENAME) {
        updateWindowTitle(msg.Values.NewName);
      } else if (msg.MessageId == FILE_VERSIONS) {
        window.open(versionsLink);
      }
    };
    
    var createEditorButton = function(editorLink) {
      var label = message("OfficeonlineEditorClient.EditButtonTitle");
      var iconClass = "uiIconEdit";
      if (editorLink.indexOf("&action=view") > -1) {
        label = message("OfficeonlineEditorClient.ViewButtonTitle");
        iconClass = "uiIconView";
      }
      return $("<li class='hidden-tabletL'><a href='" + editorLink + "' target='_blank'>"
          + "<i class='uiIconEcmsOfficeOnlineOpen uiIconEcmsLightGray " + iconClass + "'></i>" + label + "</a></li>");
      
    }

    this.initEditor = function(config) {
      extension = config.filename.substring(config.filename.lastIndexOf("."));
      updateWindowTitle(config.filename);
      versionsLink = config.versionsURL;
      $('#office_form').attr('action', config.actionURL);
      $('input[name="access_token"]').val(config.accessToken.token);
      $('input[name="access_token_ttl"]').val(config.accessToken.expires);

      var frameholder = document.getElementById('frameholder');
      var office_frame = document.createElement('iframe');
      office_frame.name = 'office_frame';
      office_frame.id = 'office_frame';
      // The title should be set for accessibility
      office_frame.title = 'Office Frame';
      // This attribute allows true fullscreen mode in slideshow view
      // when using PowerPoint's 'view' action.
      office_frame.setAttribute('allowfullscreen', 'true');
      // The sandbox attribute is needed to allow automatic redirection to the O365 sign-in page in the business user flow
      office_frame.setAttribute('sandbox',
          'allow-scripts allow-same-origin allow-forms allow-popups allow-top-navigation allow-popups-to-escape-sandbox');
      frameholder.appendChild(office_frame);
      document.getElementById('office_form').submit();
      window.addEventListener('message', handlePostMessage, false);
      editorbuttons.onEditorOpen(config.fileId, config.workspace, "officeonline");
    };
    
    this.initActivity = function(fileId, editorLink, activityId) {
      log("Initialize activity with document: " + fileId);
      // Listen to document updates
      store.subscribe(function() {
        var state = store.getState();
        if (state.type === DOCUMENT_SAVED && state.fileId === fileId) {
          addRefreshBannerActivity(activityId);
        }
      });
      subscribeDocument(fileId);
      if(editorLink != null) {
        editorbuttons.addCreateButtonFn("officeonline", function() {
          return createEditorButton(editorLink);
        });
      }
    };

    this.init = function(userId, cometdConf, userMessages) {
      if (userId == currentUserId) {
        log("Already initialized user: " + userId);
      } else if (userId) {
        currentUserId = userId;
        log("Initialize user: " + userId);
        if (userMessages) {
          messages = userMessages;
        }
        if (cometdConf) {
          cCometD.configure({
            "url" : prefixUrl + cometdConf.path,
            "exoId" : userId,
            "exoToken" : cometdConf.token,
            "maxNetworkDelay" : 30000,
            "connectTimeout" : 60000
          });
          cometdContext = {
            "exoContainerName" : cometdConf.containerName
          };
          cometd = cCometD;
        }
      } else {
        log("Cannot initialize user: " + userId);
      }
    };

    this.initPreview = function(fileId, editorLink, activityId, index) {
      log("Init preview called. FileId: " + fileId);
      var clickSelector = "#Preview" + activityId + "-" + index;
      $(clickSelector).click(function() {
        log("Initialize preview of document: " + fileId);
        // We set timeout here to avoid the case when the element is rendered but is going to be updated soon
        setTimeout(function() {
          store.subscribe(function() {
            var state = store.getState();
            if (state.type === DOCUMENT_SAVED && state.fileId === fileId) {
              addRefreshBannerPDF();
            }
          });
        }, 100);
        subscribeDocument(fileId);
      });
      if(editorLink != null) {
        editorbuttons.addCreateButtonFn("officeonline", function() {
          return createEditorButton(editorLink);
        });
      }
    };

    /**
     * Initializes JCRExplorer when a document is displayed.
     */
    this.initExplorer = function(fileId, editorLink) {
      log("Initialize explorer with document: " + fileId);
      // Listen document updated
      store.subscribe(function() {
        var state = store.getState();
        if (state.type === DOCUMENT_SAVED) {
          if (state.userId === currentUserId) {
            refreshPDFPreview();
          } else {
            addRefreshBannerPDF();
          }
        }
      });
      if (fileId != explorerFileId) {
        // We need unsubscribe from previous doc
        if (explorerFileId) {
          unsubscribeDocument(explorerFileId);
        }
        subscribeDocument(fileId);
        explorerFileId = fileId;
      }
      addEditorButtonToExplorer(editorLink);
    };

    /**
     * Sets the onClick listener for Create Document button (used in creating a new document)
     */
    this.initNewDocument = function() {
      editorWindow = window.open();
    };

    /**
     * Initializes the editor in the editorWindow. (used in creating a new document)
     */
    this.initEditorPage = function(link) {
      if (editorWindow != null) {
        if (link != null) {
          editorWindow.location = link;
        } else {
          editorWindow.close();
          editorWindow = null;
        }
      }
    };

    this.showError = function(title, message) {
      $(".officeonlineContainer").prepend(
          '<div class="alert alert-error"><i class="uiIconError"></i>' + title + ': ' + message + '</div>');
    };
  }

  var editor = new Editor();

  return editor;
})($, cCometD, Redux, editorbuttons);