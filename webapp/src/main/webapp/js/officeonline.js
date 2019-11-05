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

  /**
   * Editor core class.
   */
  function Editor() {
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
  }

  var editor = new Editor();

  return editor;
})($);