package com.google.android.exoplayer2.demo

class BitrateLimitter {

  private var maxBitrate = Long.MAX_VALUE

  fun limitMaxBitrate(bitrate: Long) {
    this.maxBitrate = bitrate
  }

  fun getMaxBitrate(): Long {
    return maxBitrate
  }
}
