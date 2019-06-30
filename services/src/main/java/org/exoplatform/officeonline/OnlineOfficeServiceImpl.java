package org.exoplatform.officeonline;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.picocontainer.Startable;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.officeonline.WOPIDiscovery.NetZone;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The Class OnlineOfficeServiceImpl.
 */
public class OnlineOfficeServiceImpl implements OnlineOfficeService, Startable {

  /** The Constant PLACEHOLDER_IS_LICENSED_USER. */
  public static final String                 PLACEHOLDER_IS_LICENSED_USER       = "IsLicensedUser";

  /** The Constant PLACEHOLDER_IS_LICENSED_USER_VALUE. */
  public static final String                 PLACEHOLDER_IS_LICENSED_USER_VALUE = "1";

  /** The Constant DISCOVERY_URL_PROPERTY. */
  private static final String                DISCOVERY_URL_PROPERTY             = "discovery-url";

  /** The Constant LOG. */
  protected static final Log                 LOG                                =
                                                 ExoLogger.getLogger(OfficeOnlineEditorServiceImpl.class);

  /** The discovery URL. */
  private final String                       discoveryURL;

  /** The supported app names. */
  protected final List<String>               supportedAppNames                  = Arrays.asList("Word", "Excel", "PowerPoint");

  /** The extension action UR ls. */
  // extension => wopi action => wopi action url
  protected Map<String, Map<String, String>> extensionActionURLs                = new HashMap<>();

  /** The proof key. */
  protected PublicKey                        proofKey;

  /** The old proof key. */
  protected PublicKey                        oldProofKey;

  /**
   * Instantiates a new online office service impl.
   *
   * @param params the params
   */
  public OnlineOfficeServiceImpl(InitParams params) {
    discoveryURL = params.getValueParam(DISCOVERY_URL_PROPERTY).getValue();
  }

  /**
   * Start.
   */
  @Override
  public void start() {
    try {
      loadDiscovery();
    } catch (OfficeOnlineEditorException e) {
      LOG.error("Error while loading WOPI discovery {}", e.getMessage());
    }
  }

  /**
   * Stop.
   */
  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

  /**
   * Load discovery.
   * @throws OfficeOnlineEditorException 
   */
  protected void loadDiscovery() throws OfficeOnlineEditorException {
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

    netZone.getApps().stream().filter(app -> supportedAppNames.contains(app.getName())).forEach(this::registerApp);
    LOG.debug("Successfully loaded WOPI discovery: WOPI enabled");

    WOPIDiscovery.ProofKey pk = discovery.getProofKey();
    proofKey = ProofKeyHelper.getPublicKey(pk.getModulus(), pk.getExponent());
    oldProofKey = ProofKeyHelper.getPublicKey(pk.getOldModulus(), pk.getOldExponent());
    LOG.debug("Registered proof key: {}", proofKey);
    LOG.debug("Registered old proof key: {}", oldProofKey);
  }

  /**
   * Fetch discovery.
   *
   * @return the byte[]
   */
  protected byte[] fetchDiscovery() {
    if (discoveryURL == null) {
      LOG.warn("No WOPI discovery URL configured, cannot fetch discovery. Please configure the '{}' property.",
               DISCOVERY_URL_PROPERTY);
      return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    LOG.debug("Fetching WOPI dicovery from discovery URL {}", discoveryURL);
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
    HttpGet request = new HttpGet(discoveryURL);
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
   * Register app.
   *
   * @param app the app
   */
  protected void registerApp(WOPIDiscovery.App app) {
    app.getActions().forEach(action -> {
      extensionActionURLs.computeIfAbsent(action.getExt(), k -> new HashMap<>())
                         .put(action.getName(),
                              String.format("%s%s=%s&",
                                            action.getUrl().replaceFirst("<.*$", ""),
                                            PLACEHOLDER_IS_LICENSED_USER,
                                            PLACEHOLDER_IS_LICENSED_USER_VALUE));
    });
  }

}
