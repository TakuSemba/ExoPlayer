package com.google.android.exoplayer2.demo

import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.util.Clock

class LimitTrackSelection @JvmOverloads constructor(
  group: TrackGroup,
  tracks: IntArray,
  bandwidthMeter: BandwidthMeter,
  minDurationForQualityIncreaseMs: Long = DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS.toLong(),
  maxDurationForQualityDecreaseMs: Long = DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS.toLong(),
  minDurationToRetainAfterDiscardMs: Long = DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS.toLong(),
  bandwidthFraction: Float = DEFAULT_BANDWIDTH_FRACTION,
  bufferedFractionToLiveEdgeForQualityIncrease: Float = DEFAULT_BUFFERED_FRACTION_TO_LIVE_EDGE_FOR_QUALITY_INCREASE,
  minTimeBetweenBufferReevaluationMs: Long = DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS,
  clock: Clock = Clock.DEFAULT,
  private val bitrateLimitter: BitrateLimitter
) : AdaptiveTrackSelection(
  group,
  tracks,
  bandwidthMeter,
  minDurationForQualityIncreaseMs,
  maxDurationForQualityDecreaseMs,
  minDurationToRetainAfterDiscardMs,
  bandwidthFraction,
  bufferedFractionToLiveEdgeForQualityIncrease,
  minTimeBetweenBufferReevaluationMs,
  clock
) {

  class Factory @JvmOverloads constructor(
    private val minDurationForQualityIncreaseMs: Int = DEFAULT_MIN_DURATION_FOR_QUALITY_INCREASE_MS,
    private val maxDurationForQualityDecreaseMs: Int = DEFAULT_MAX_DURATION_FOR_QUALITY_DECREASE_MS,
    private val minDurationToRetainAfterDiscardMs: Int = DEFAULT_MIN_DURATION_TO_RETAIN_AFTER_DISCARD_MS,
    private val bandwidthFraction: Float = DEFAULT_BANDWIDTH_FRACTION,
    private val bufferedFractionToLiveEdgeForQualityIncrease: Float = DEFAULT_BUFFERED_FRACTION_TO_LIVE_EDGE_FOR_QUALITY_INCREASE,
    private val minTimeBetweenBufferReevaluationMs: Long = DEFAULT_MIN_TIME_BETWEEN_BUFFER_REEVALUTATION_MS,
    private val clock: Clock = Clock.DEFAULT,
    private val bitrateLimitter: BitrateLimitter
  ) : AdaptiveTrackSelection.Factory(
    minDurationForQualityIncreaseMs,
    maxDurationForQualityDecreaseMs,
    minDurationToRetainAfterDiscardMs,
    bandwidthFraction,
    bufferedFractionToLiveEdgeForQualityIncrease,
    minTimeBetweenBufferReevaluationMs,
    clock
  ) {

    override fun createAdaptiveTrackSelection(
      group: TrackGroup,
      bandwidthMeter: BandwidthMeter,
      tracks: IntArray
    ): AdaptiveTrackSelection {
      return LimitTrackSelection(
        group,
        tracks,
        bandwidthMeter,
        minDurationForQualityIncreaseMs.toLong(),
        maxDurationForQualityDecreaseMs.toLong(),
        minDurationToRetainAfterDiscardMs.toLong(),
        bandwidthFraction,
        bufferedFractionToLiveEdgeForQualityIncrease,
        minTimeBetweenBufferReevaluationMs,
        clock,
        bitrateLimitter
      )
    }
  }

  override fun canSelectFormat(
    format: Format,
    trackBitrate: Int,
    playbackSpeed: Float,
    effectiveBitrate: Long
  ): Boolean {
    val limitedEffectiveBitrate = Math.min(bitrateLimitter.getMaxBitrate(), effectiveBitrate)
    return super.canSelectFormat(format, trackBitrate, playbackSpeed, limitedEffectiveBitrate)
  }
}
