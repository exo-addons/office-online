package org.exoplatform.officeonline;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.sun.star.uno.RuntimeException;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The Class WOPIAvailabilityCheckerPlugin is used to check WOPI availability status.
 */
public class WOPIAvailabilityCheckerPlugin extends BaseComponentPlugin {

  /** The Constant LOG. */
  protected static final Log         LOG             = ExoLogger.getLogger(WOPIAvailabilityCheckerPlugin.class);

  /** The discovery url. */
  protected String                   wopiCheckUrl;

  /** The Constant WOPI_HOST_URL_PARAM. */
  protected static final String      WOPI_CHECK_URL_PARAM  = "check-url";

  /** The WOPI available status. */
  protected boolean                  available       = false;

  /** The executor for refreshing. */
  protected ScheduledExecutorService refreshExecutor = Executors.newScheduledThreadPool(1);

  /**
   * Instantiates a new WOPI Availability Checker Plugin
   *
   * @param params the params
   */
  public WOPIAvailabilityCheckerPlugin(InitParams params) {
    ValueParam wopiCheckUrlParam = params.getValueParam(WOPI_CHECK_URL_PARAM);
    String val = wopiCheckUrlParam != null ? wopiCheckUrlParam.getValue() : null;
    if (val == null || (val = val.trim()).isEmpty()) {
      throw new RuntimeException(WOPI_CHECK_URL_PARAM + " parameter is required.");
    } else {
      this.wopiCheckUrl = val;
    }
  }

  /**
   * Start.
   */
  public void start() {
    // Refresh availability status every 10 minutes
    refreshExecutor.scheduleAtFixedRate(() -> {
      available = checkWOPIAvailability();
    }, 0, 10, TimeUnit.MINUTES);
  }

  /**
   * Stop.
   */
  public void stop() {
    refreshExecutor.shutdown();
  }

  /**
   * Checks if is WOPI available.
   *
   * @return true, if is WOPI available
   */
  public boolean isWOPIAvailable() {
    return available;
  }

  /**
   * Check WOPI availability.
   *
   * @return true, if successful
   */
  protected boolean checkWOPIAvailability() {
    try {
      URL url = new URL(wopiCheckUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.connect();
      if (connection.getResponseCode() == 200) {
        return true;
      }
    } catch (Exception e) {
      LOG.warn("Cannot check WOPI host availability: ", e.getMessage());
    }
    return false;
  }

}
