package org.exoplatform.officeonline;

/**
 * The Interface OfficeOnlineEditorService.
 */
public interface OfficeOnlineEditorService {

  
  /**
   * Verify proof key.
   *
   * @param proofKeyHeader the proof key header
   * @param oldProofKeyHeader the old proof key header
   * @param url the url
   * @param accessToken the access token
   * @param timestampHeader the timestamp header
   * @return true, if successful
   */
  boolean verifyProofKey(String proofKeyHeader, String oldProofKeyHeader, String url, String accessToken, String timestampHeader);
}
