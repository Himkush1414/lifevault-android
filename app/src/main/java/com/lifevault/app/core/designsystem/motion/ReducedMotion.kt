package com.lifevault.app.core.designsystem.motion

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * True when the user has disabled system animations (Settings > Accessibility >
 * Remove animations, or a developer-option animation scale of 0). Section 4.6 requires
 * every slide/shared-element/shimmer to fall back to an instant cross-fade (≤100ms) when
 * this is true.
 */
@Composable
fun rememberIsReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    var reduced by remember { mutableStateOf(context.isReducedMotionEnabled()) }

    DisposableEffect(context) {
        val handler = Handler(Looper.getMainLooper())
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                reduced = context.isReducedMotionEnabled()
            }
        }
        val resolver = context.contentResolver
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.TRANSITION_ANIMATION_SCALE),
            false,
            observer,
        )
        resolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return reduced
}

private fun Context.isReducedMotionEnabled(): Boolean {
    val transitionScale = Settings.Global.getFloat(
        contentResolver,
        Settings.Global.TRANSITION_ANIMATION_SCALE,
        1f,
    )
    val animatorScale = Settings.Global.getFloat(
        contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    return transitionScale == 0f || animatorScale == 0f
}
