package org.exoplatform.officeonline;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.sun.star.uno.RuntimeException;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.officeonline.WOPIDiscovery.NetZone;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The Class WOPIDiscoveryPlugin.
 */
public class WOPIDiscoveryPlugin extends BaseComponentPlugin {

  /** The Constant LOG. */
  protected static final Log                      LOG                                =
                                                      ExoLogger.getLogger(WOPIDiscoveryPlugin.class);

  /** The Constant DISCOVERY_URL. */
  protected static final String                   DISCOVERY_URL                      = "discovery-url";

  /** The Constant PLACEHOLDER_IS_LICENSED_USER. */
  protected static final String                   PLACEHOLDER_IS_LICENSED_USER       = "IsLicensedUser";

  /** The Constant PLACEHOLDER_IS_LICENSED_USER_VALUE. */
  protected static final String                   PLACEHOLDER_IS_LICENSED_USER_VALUE = "1";

  /** The Constant CACHE_NAME. */
  protected static final String                   CACHE_NAME                         = "officeonline.discovery.Cache".intern();

  /** The supported app names. */
  protected final List<String>                    supportedAppNames                  =
                                                                    Arrays.asList("Word", "Excel", "PowerPoint", "WopiTest");

  /** The proof key. */
  protected PublicKey                             proofKey;

  /** The old proof key. */
  protected PublicKey                             oldProofKey;

  /** The discovery url. */
  protected String                                discoveryUrl;

  /** The extension action UR ls. */
  // extension => wopi action => wopi action url
  protected ExoCache<String, Map<String, String>> extensionActionURLs;

  /** The executor for refreshing. */
  protected ScheduledExecutorService              refreshExecutor                    = Executors.newScheduledThreadPool(1);

  /**
   * Instantiates a new WOPI discovery service.
   *
   * @param cacheService the cache service
   * @param params the params
   */
  public WOPIDiscoveryPlugin(CacheService cacheService, InitParams params) {
    ValueParam discoveryURLParam = params.getValueParam(DISCOVERY_URL);
    String val = discoveryURLParam != null ? discoveryURLParam.getValue() : null;
    if (val == null || (val = val.trim()).isEmpty()) {
      throw new RuntimeException(DISCOVERY_URL + " parameter is required.");
    } else {
      this.discoveryUrl = val;
    }
    this.extensionActionURLs = cacheService.getCacheInstance(CACHE_NAME);
  }

  /**
   * Gets the action url.
   *
   * @param extension the extension
   * @param action the action
   * @return the action url
   */
  public String getActionUrl(String extension, String action) {
    if (extensionActionURLs.getCacheSize() == 0) {
      loadDiscovery();
    }

    Map<String, String> map = extensionActionURLs.get(extension);
    if (map != null && !map.isEmpty()) {
      return map.get(action);
    }
    LOG.warn("Cannot find action url for {} extension and {} action", extension, action);
    return null;
  }

  /**
   * Gets the proof key.
   *
   * @return the proof key
   */
  public PublicKey getProofKey() {
    return proofKey;
  }

  /**
   * Gets the old proof key.
   *
   * @return the old proof key
   */
  public PublicKey getOldProofKey() {
    return oldProofKey;
  }

  /**
   * Start.
   */
  public void start() {
    loadDiscovery();
    // Refresh discovery every 5 minutes (for testing only). 12-24 hours is recommended
    refreshExecutor.scheduleAtFixedRate(() -> loadDiscovery(), 5, 5, TimeUnit.MINUTES);
  }

  /**
   * Stop.
   */
  public void stop() {
    refreshExecutor.shutdown();
  }

  /**
   * Load discovery.
   */
  protected void loadDiscovery() {
    byte[] discoveryBytes = fetchDiscovery();
    WOPIDiscovery discovery;
    try {
      discovery = WOPIDiscovery.read(discoveryBytes);
    } catch (IOException e) {
      LOG.error("Error while reading WOPI discovery {}", e.getMessage());
      return;
    }

    NetZone netZone = discovery.getNetZone();
    if (netZone == null) {
      LOG.error("Invalid WOPI discovery, no net-zone element");
      return;
    }

    registerApps(netZone.getApps());

    WOPIDiscovery.ProofKey pk = discovery.getProofKey();
    proofKey = ProofKeyHelper.getPublicKey(pk.getModulus(), pk.getExponent());
    oldProofKey = ProofKeyHelper.getPublicKey(pk.getOldModulus(), pk.getOldExponent());

    if (LOG.isDebugEnabled()) {
      LOG.debug("Successfully loaded WOPI discovery: WOPI enabled");
      LOG.debug("Registered proof key: {}", proofKey);
      LOG.debug("Registered old proof key: {}", oldProofKey);
    }
  }

  /**
   * Fetch discovery.
   *
   * @return the byte[]
   */
  protected byte[] fetchDiscovery() {
    if (discoveryUrl == null || discoveryUrl.isEmpty()) {
      throw new RuntimeException("DiscoveryUrl is not specified");
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("Fetching WOPI dicovery from discovery URL {}", discoveryUrl);
    }

    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
    HttpGet request = new HttpGet(discoveryUrl);
    try (CloseableHttpClient httpClient = httpClientBuilder.build();
        CloseableHttpResponse response = httpClient.execute(request);
        InputStream is = response.getEntity().getContent()) {
      return IOUtils.toByteArray(is);
    } catch (IOException e) {
      LOG.error("Error while fetching WOPI discovery: {}", e.getMessage());
      return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

  }

  /**
   * Register apps.
   *
   * @param apps the apps
   */
  protected void registerApps(List<WOPIDiscovery.App> apps) {
    Map<String, Map<String, String>> actionURLs = new HashMap<>();
    apps.stream().filter(app -> supportedAppNames.contains(app.getName())).forEach(app -> {
      app.getActions().forEach(action -> {
        actionURLs.computeIfAbsent(action.getExt(), k -> new HashMap<>())
                  .put(action.getName(),
                       String.format("%s%s=%s&",
                                     action.getUrl().replaceFirst("<.*$", ""),
                                     PLACEHOLDER_IS_LICENSED_USER,
                                     PLACEHOLDER_IS_LICENSED_USER_VALUE));
      });
    });
    extensionActionURLs.clearCache();
    actionURLs.forEach((key, value) -> {
      if (key != null) {
        extensionActionURLs.put(key, value);
      }
    });
  }

}
