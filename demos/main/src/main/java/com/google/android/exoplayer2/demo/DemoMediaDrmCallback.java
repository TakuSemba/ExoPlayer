package com.google.android.exoplayer2.demo;

import com.google.android.exoplayer2.drm.ExoMediaDrm;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import java.io.IOException;
import java.util.UUID;
import org.json.JSONObject;

public final class DemoMediaDrmCallback implements MediaDrmCallback {

  private final byte[] videoKeyResponse;
  private final byte[] audioKeyResponse;

  public DemoMediaDrmCallback() {
    videoKeyResponse =
        "{\"keys\":[{\"kty\":\"oct\",\"k\":\"7s2ytUnwKnyXzlDBf0lMoA\",\"kid\":\"x3/uNeUf1hWnuRr8sQkcXg\"}],\"type\":\"temporary\"}"
            .getBytes();
    audioKeyResponse =
        "{\"keys\":[{\"kty\":\"oct\",\"k\":\"mrt6tsxK07hsIZPa2x54bA\",\"kid\":\"BF9+zDWEjtezwBLqdhRCLw\"}],\"type\":\"temporary\"}"
            .getBytes();
  }

  @Override public byte[] executeProvisionRequest(UUID uuid, ExoMediaDrm.ProvisionRequest request)
      throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override public byte[] executeKeyRequest(UUID uuid, ExoMediaDrm.KeyRequest request)
      throws Exception {
    JSONObject requestBody = new JSONObject(new String(request.getData()));
    switch (requestBody.getJSONArray("kids").getString(0)) {
      case "x3_uNeUf1hWnuRr8sQkcXg":
        return videoKeyResponse;
      case "BF9-zDWEjtezwBLqdhRCLw":
        return audioKeyResponse;
      default:
        throw new UnsupportedOperationException();
    }
  }
}
