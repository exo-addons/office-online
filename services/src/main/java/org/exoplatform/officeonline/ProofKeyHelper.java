package org.exoplatform.officeonline;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;

import javax.xml.bind.DatatypeConverter;

/**
 * The Class ProofKeyHelper.
 */
public class ProofKeyHelper {

  /** The Constant KEY_FACTORY_ALGORITHM. */
  public static final String KEY_FACTORY_ALGORITHM = "RSA";

  /** The Constant SIGNATURE_ALGORITHM. */
  public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

  /** The Constant EPOCH_IN_TICKS. */
  public static final long EPOCH_IN_TICKS = 621355968000000000L; // January 1, 1970 (start of Unix epoch) in "ticks"

  /**
   * Instantiates a new proof key helper.
   */
  private ProofKeyHelper() {
      // helper class
  }

  /**
   * Gets the public key.
   *
   * @param modulus the modulus
   * @param exponent the exponent
   * @return the public key
   */
  public static PublicKey getPublicKey(String modulus, String exponent) {
      BigInteger mod = new BigInteger(1, DatatypeConverter.parseBase64Binary(modulus));
      BigInteger exp = new BigInteger(1, DatatypeConverter.parseBase64Binary(exponent));
      KeyFactory factory;
      try {
          factory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
          KeySpec ks = new RSAPublicKeySpec(mod, exp);
          return factory.generatePublic(ks);
      } catch (GeneralSecurityException e) {
          throw new RuntimeException(e);
      }
  }

  /**
   * Gets the expected proof bytes.
   *
   * @param url the url
   * @param accessToken the access token
   * @param timestamp the timestamp
   * @return the expected proof bytes
   */
  public static byte[] getExpectedProofBytes(String url, String accessToken, long timestamp) {
      byte[] accessTokenBytes = accessToken.getBytes(StandardCharsets.UTF_8);
      byte[] hostUrlBytes = url.toUpperCase().getBytes(StandardCharsets.UTF_8);
      ByteBuffer byteBuffer = ByteBuffer.allocate(4 + accessTokenBytes.length + 4 + hostUrlBytes.length + 4 + 8);
      byteBuffer.putInt(accessTokenBytes.length);
      byteBuffer.put(accessTokenBytes);
      byteBuffer.putInt(hostUrlBytes.length);
      byteBuffer.put(hostUrlBytes);
      byteBuffer.putInt(8);
      byteBuffer.putLong(timestamp);
      return byteBuffer.array();
  }

  /**
   * Verify proof key.
   *
   * @param key the key
   * @param proofKeyHeader the proof key header
   * @param expectedProofBytes the expected proof bytes
   * @return true, if successful
   */
  public static boolean verifyProofKey(PublicKey key, String proofKeyHeader, byte[] expectedProofBytes) {
      try {
          Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
          verifier.initVerify(key);
          verifier.update(expectedProofBytes);
          byte[] signedProof = DatatypeConverter.parseBase64Binary(proofKeyHeader);
          return verifier.verify(signedProof);
      } catch (GeneralSecurityException e) {
          return false;
      }
  }

  /**
   * Checks that the given {@code timestamp} is no more than 20 minutes old.
   *
   * @param timestamp the timestamp
   * @return true, if successful
   */
  public static boolean verifyTimestamp(long timestamp) {
      long ticks = timestamp - EPOCH_IN_TICKS; // ticks
      long ms = ticks / 10_000; // milliseconds
      Instant instant = Instant.ofEpochMilli(ms);
      Duration duration = Duration.between(instant, Instant.now());
      return duration.compareTo(Duration.ofMinutes(20)) <= 0;
  }

}