package com.google.android.exoplayer2.upstream;

import android.annotation.SuppressLint;
import android.net.Uri;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * VideoKeyUriSource provides UriDataSource implementation
 * to provide TS encryption key from URI texts.
 */
class VideoKeyUriSource implements DataSource {

  private static final SecretKeySpec TS_COMMON_KEY =
      new SecretKeySpec("DiCUaHk2VTS7MLEA&n9YHR>'D^gT1zbs".getBytes(), "hmacSHA256");

  private String deviceId;
  private DataSpec dataSpec;
  private byte[] encryptionKey;
  private int bytesRemaining;

  VideoKeyUriSource(String deviceId) {
    this.deviceId = deviceId;
  }

  @Override public Uri getUri() {
    return dataSpec.uri;
  }

  @Override public long open(DataSpec dataSpec) throws IOException {
    this.dataSpec = dataSpec;
    List<String> segments = dataSpec.uri.getPathSegments();
    if (segments.size() < 2) {
      throw new IOException("URI Path segments length should be greater than 2");
    }
    if (dataSpec.uri.getHost().equals("v2")) {
      String slotId = segments.get(1);
      byte[] publicKey = Base58.decode(segments.get(2));
      SecretKeySpec commonSecret = getCommonSecretV2();
      byte[] decryptionKey = hmacSha256((slotId + deviceId).getBytes(), commonSecret);
      encryptionKey = getEncryptionKey(publicKey, decryptionKey);
      bytesRemaining = encryptionKey.length;
      return bytesRemaining;
    }

    String slotId = segments.get(0);
    byte[] publicKey = decodeHex(segments.get(1));
    byte[] decryptionKey = hmacSha256(slotId.getBytes(), TS_COMMON_KEY);
    encryptionKey = getEncryptionKey(publicKey, decryptionKey);
    bytesRemaining = encryptionKey.length;
    return bytesRemaining;
  }

  @Override public void close() throws IOException {
    encryptionKey = null;
  }

  @Override public int read(byte[] buffer, int offset, int readLength) throws IOException {
    if (bytesRemaining == 0) {
      return -1;
    }
    int length = Math.min(encryptionKey.length, readLength);
    System.arraycopy(encryptionKey, offset, buffer, 0, length);
    bytesRemaining -= length;
    return length;
  }

  static SecretKeySpec getCommonSecretV2() {
    byte[] src = new byte[] {
        -44, -73, 24, -69, -70, -100, -5, 125, 1, -110, -91, -113, -98, 45, 20, 106, -4, 93, -78,
        -98, 67, 82, -34, 5, -4, 76, -14, -63, 0, 88, 4, -69
    };
    byte[] dst = new byte[32];
    RC4 rc4 = new RC4(
        new byte[] { -37, -104, -88, -25, -50, -54, 52, 36, -39, 117, 40, 15, -112, -67, 3, -18 });
    rc4.update(src, dst);
    return new SecretKeySpec(dst, "hmacSHA256");
  }

  /**
   * Get encryption key from source bytes and key bytes.
   *
   * @param source An array binary of source
   * @param key An array binary of encryption key
   * @return Decrypted TS key
   * @throws IOException
   */
  static byte[] getEncryptionKey(final byte[] source, final byte[] key) throws IOException {
    SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
    try {
      @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, keySpec);
      return cipher.doFinal(source);
    } catch (GeneralSecurityException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  /**
   * Calculate HMac-SHA256 hash with private common key.
   *
   * @param source An array of binary.
   * @return Hashed bytes
   * @throws IOException
   */
   static byte[] hmacSha256(final byte[] source, SecretKeySpec spec) throws IOException {
    try {
      Mac mac = Mac.getInstance(spec.getAlgorithm());
      mac.init(spec);
      return mac.doFinal(source);
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  /**
   * decodeHex extracted from Apache Commons Codec library.
   *
   * @param text A string containing hexadecimal digits
   * @return A byte array containing binary data decoded from the supplied char array.
   */
  private static byte[] decodeHex(final String text) throws IOException {

    final char[] data = text.toCharArray();
    final int len = data.length;

    if ((len & 0x01) != 0) {
      throw new IOException("Odd number of characters.");
    }
    final byte[] out = new byte[len >> 1];
    // two characters form the hex value.
    for (int i = 0, j = 0; j < len; i++) {
      int f = toDigit(data[j], j) << 4;
      j++;
      f = f | toDigit(data[j], j);
      j++;
      out[i] = (byte) (f & 0xFF);
    }

    return out;
  }

  private static int toDigit(final char ch, final int index) throws IOException {
    final int digit = Character.digit(ch, 16);
    if (digit == -1) {
      throw new IOException("Illegal hexadecimal character " + ch + " at index " + index);
    }
    return digit;
  }
}
