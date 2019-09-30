package org.exoplatform.officeonline;

import org.picocontainer.Startable;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The Class OfficeOnlineEditorServiceImpl.
 */
public class OfficeOnlineEditorServiceImpl implements OfficeOnlineEditorService, Startable {

  /** The Constant LOG. */
  protected static final Log LOG = ExoLogger.getLogger(OfficeOnlineEditorServiceImpl.class);
  
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
    
    LOG.info("EXCEL EDIT: " + excelEdit);
    LOG.info("EXCEL VIEW: " + excelView);
    LOG.info("WORD EDIT: " + wordEdit);
    LOG.info("WORD VIEW: " + wordView);
    LOG.info("PP EDIT: " + powerPointEdit);
    LOG.info("PP VIEW: " + powerPointView);
  }

  /**
   * Stop.
   */
  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

}
