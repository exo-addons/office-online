/**
 * Office Online Editor client.
 */
(function($) {
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
    // TODO: i18n
    var label = "Edit in MS";
    if (editorLink.indexOf("&action=view") > -1) {
      label = "View in MS";
    }
    return "<li class='hidden-tabletL'><a href='" + editorLink + "' target='_blank'>"
        + "<i class='uiIconEcmsOfficeOnlineOpen uiIconEcmsLightGray uiIconEdit'></i>" + label + "</a></li>";
  };

  var getNoPreviewEditorButton = function(editorLink) {
    // TODO: i18n
    var label = "Edit in MS";
    if (editorLink.indexOf("&action=view") > -1) {
      label = "View in MS";
    }
    return "<a class='btn editInOfficeOnline hidden-tabletL' href='#' onclick='javascript:window.open(\"" + editorLink + "\");'>"
        + "<i class='uiIconEcmsOfficeOnlineOpen uiIconEcmsLightGray uiIconEdit'></i>" + label + "</a>";
  };

  /**
   * Adds the 'Edit' button to No-preview screen (from the activity stream) when it's loaded.
   */
  var tryAddEditorButtonNoPreview = function(editorLink, attempts, delay) {
    var $elem = $("#documentPreviewContainer .navigationContainer.noPreview");
    if ($elem.length == 0 || !$elem.is(":visible")) {
      if (attempts > 0) {
        setTimeout(function() {
          tryAddEditorButtonNoPreview(editorLink, attempts - 1, delay);
        }, delay);
      } else {
        log("Cannot find .noPreview element");
      }
    } else if ($elem.find("a.editInOfficeOnline").length == 0) {
      var $detailContainer = $elem.find(".detailContainer");
      var $downloadBtn = $detailContainer.find(".uiIconDownload").closest("a.btn");
      if ($downloadBtn.length != 0) {
        $downloadBtn.after(getNoPreviewEditorButton(editorLink));
      } else {
        $detailContainer.append(getNoPreviewEditorButton(editorLink));
      }
    }
  };
  /**
   * Adds the 'Edit' button to a preview (from the activity stream) when it's loaded.
   */
  var tryAddEditorButtonToPreview = function(editorLink, attempts, delay) {
    var $elem = $("#uiDocumentPreview .previewBtn");
    if ($elem.length == 0 || !$elem.is(":visible")) {
      if (attempts > 0) {
        setTimeout(function() {
          tryAddEditorButtonToPreview(editorLink, attempts - 1, delay);
        }, delay);
      } else {
        log("Cannot find element " + $elem);
      }
    } else {
      $elem.append("<div class='editInOfficeOnline'>" + getEditorButton(editorLink) + "</div>");
    }
  };

  /**
   * Ads the 'Edit' button to a preview (opened from the activity stream).
   */
  var addEditorButtonToPreview = function(editorLink, clickSelector) {
    $(clickSelector).click(function() {
      // We set timeout here to avoid the case when the element is rendered but is going to be updated soon
      setTimeout(function() {
        tryAddEditorButtonToPreview(editorLink, 100, 100);
        // We need wait for about 2min when doc cannot generate its preview
        tryAddEditorButtonNoPreview(editorLink, 600, 250);
      }, 100);
    });
  };

  /**
   * Ads the 'Edit Online' button to the JCRExplorer when a document is displayed.
   */
  var addEditorButtonToExplorer = function(editorLink) {
    var $button = $("#UIJCRExplorer #uiActionsBarContainer i.uiIconEcmsOfficeOnlineOpen");
    // TODO: i18n
    if (editorLink.indexOf("&action=view") > -1) {
      $button.parent().text("View in MS");
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

  /**
   * Editor core class.
   */
  function Editor() {

    var editorWindow;

    this.initEditor = function(accessToken, actionURL) {

      $('#office_form').attr('action', actionURL);
      $('input[name="access_token"]').val(accessToken.token);
      $('input[name="access_token_ttl"]').val(accessToken.expires);

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
    };

    this.showError = function(title, message) {
      // TODO: show as a popup/notification
      alert(title + " " + message);
    };

    this.initActivity = function(fileId, editorLink, activityId) {
      if (editorLink) {
        $("#activityContainer" + activityId).find("div[id^='ActivityContextBox'] > .actionBar .statusAction.pull-left").append(
            getEditorButton(editorLink));
      }
    };

    this.initPreview = function(docId, editorLink, clickSelector) {
      log("Init preview called");
      $(clickSelector).click(function() {
        log("Initialize preview " + clickSelector + " of document: " + docId);
      });
      if (editorLink) {
        addEditorButtonToPreview(editorLink, clickSelector);
      }
    };

    /**
     * Initializes JCRExplorer when a document is displayed.
     */
    this.initExplorer = function(docId, editorLink) {
      log("Initialize explorer with document: " + docId);
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
  }

  var editor = new Editor();

  return editor;
})($);