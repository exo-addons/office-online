package org.exoplatform.officeonline;

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
    LOG.debug("Office Online Editor Service started");
    // Only for testing purposes
    String excelEdit = discoveryService.getActionUrl("xlsx", "edit");
    String excelView = discoveryService.getActionUrl("xlsx", "view");
    String wordEdit = discoveryService.getActionUrl("docx", "edit");
    String wordView = discoveryService.getActionUrl("docx", "view");
    String powerPointEdit = discoveryService.getActionUrl("pptx", "edit");
    String powerPointView = discoveryService.getActionUrl("pptx", "view");

    LOG.debug("Excel edit URL: " + excelEdit);
    LOG.debug("Excel view URL: " + excelView);
    LOG.debug("Word edit URL: " + wordEdit);
    LOG.debug("Excel view URL: " + wordView);
    LOG.debug("PowerPoint edit URL: " + powerPointEdit);
    LOG.debug("PowerPoint view URL: " + powerPointView);
  }

  /**
   * {@inheritDoc}
   */
  @Override
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
