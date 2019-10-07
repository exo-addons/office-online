package org.exoplatform.officeonline;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The Class OfficeOnlineEditorServiceImpl.
 */
public class OfficeOnlineEditorServiceImpl implements OfficeOnlineEditorService, Startable {

  /** The Constant LOG. */
  protected static final Log     LOG = ExoLogger.getLogger(OfficeOnlineEditorServiceImpl.class);

  /** The discovery service. */
  protected WOPIDiscoveryService discoveryService;

  /**
   * Instantiates a new office online editor service impl.
   *
   * @param discoveryService the discovery service
   */
  public OfficeOnlineEditorServiceImpl(WOPIDiscoveryService discoveryService) {
    this.discoveryService = discoveryService;
  }

  /**
   * Start.
   */
  @Override
  public void start() {

    LOG.info("OFFICE ONLINE EDITOR SERVICE STARTED");
    discoveryService.loadDiscovery();
    // Testing
    String excelEdit = discoveryService.getActionUrl("xlsx", "edit");
    String excelView = discoveryService.getActionUrl("xlsx", "view");
    String wordEdit = discoveryService.getActionUrl("docx", "edit");
    String wordView = discoveryService.getActionUrl("docx", "view");
    String powerPointEdit = discoveryService.getActionUrl("pptx", "edit");
    String powerPointView = discoveryService.getActionUrl("pptx", "view");

    // Refresh Discovery
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    executor.scheduleAtFixedRate(() -> discoveryService.loadDiscovery(), 2, 2, TimeUnit.MINUTES);
    
    LOG.info("EXCEL EDIT: " + excelEdit);
    LOG.info("EXCEL VIEW: " + excelView);
    LOG.info("WORD EDIT: " + wordEdit);
    LOG.info("WORD VIEW: " + wordView);
    LOG.info("PP EDIT: " + powerPointEdit);
    LOG.info("PP VIEW: " + powerPointView);
  }

  public boolean verifyProofKey(String proofKeyHeader,
                                String oldProofKeyHeader,
                                String url,
                                String accessToken,
                                String timestampHeader) {
    if (StringUtils.isBlank(proofKeyHeader)) {
      return true; // assume valid
    }

    long timestamp = Long.parseLong(timestampHeader);
    if (!ProofKeyHelper.verifyTimestamp(timestamp)) {
      return false;
    }

    byte[] expectedProofBytes = ProofKeyHelper.getExpectedProofBytes(url, accessToken, timestamp);
    // follow flow from https://wopi.readthedocs.io/en/latest/scenarios/proofkeys.html#verifying-the-proof-keys
    boolean res = ProofKeyHelper.verifyProofKey(discoveryService.getProofKey(), proofKeyHeader, expectedProofBytes);
    if (!res && StringUtils.isNotBlank(oldProofKeyHeader)) {
      res = ProofKeyHelper.verifyProofKey(discoveryService.getProofKey(), oldProofKeyHeader, expectedProofBytes);
      if (!res) {
        res = ProofKeyHelper.verifyProofKey(discoveryService.getOldProofKey(), proofKeyHeader, expectedProofBytes);
      }
    }
    return res;
  }

  /**
   * Stop.
   */
  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

}
