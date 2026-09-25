package com.lifevault.app.core.designsystem.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing

/** One (curve, duration) pair from the Section 4.6 motion token table. */
data class MotionToken(val easing: Easing, val durationMillis: Int)

/**
 * M3 easing and duration tokens (Section 4.6). Defined once here; feature code must
 * reference these, never a raw `tween(...)` with hand-picked numbers.
 */
object Motion {
    val emphasized = MotionToken(CubicBezierEasing(0.2f, 0f, 0f, 1f), 500)
    val emphasizedDecelerate = MotionToken(CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f), 400)
    val emphasizedAccelerate = MotionToken(CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f), 200)
    val standard = MotionToken(CubicBezierEasing(0.2f, 0f, 0f, 1f), 300)
    val standardDecelerate = MotionToken(CubicBezierEasing(0f, 0f, 0f, 1f), 250)
    val standardAccelerate = MotionToken(CubicBezierEasing(0.3f, 0f, 1f, 1f), 200)
    val short = MotionToken(LinearEasing, 120)

    /** Never exceed this for any single transition (Section 4.6). */
    const val MAX_DURATION_MILLIS = 500

    /** Shimmer loop period for skeleton loading states. */
    const val SHIMMER_LOOP_MILLIS = 1200

    /** Cross-fade used instead of a shared element / slide when [ReducedMotion] is on. */
    const val REDUCED_MOTION_CROSSFADE_MILLIS = 100

    /** Loading spinners only appear after this delay, to avoid flicker on fast operations. */
    const val LOADING_INDICATOR_DELAY_MILLIS = 300L
}
