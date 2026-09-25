package com.lifevault.app.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.lifevault.app.core.designsystem.motion.Motion
import com.lifevault.app.core.designsystem.motion.rememberIsReducedMotionEnabled

/**
 * A shimmering placeholder block for loading states (Section 3.3 S12 OCR review,
 * Section 4.6: 1200ms loop). Freezes to a static tone instead of animating when
 * [rememberIsReducedMotionEnabled] is true.
 */
@Composable
fun SkeletonShimmer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val highlightColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val reducedMotion = rememberIsReducedMotionEnabled()

    if (reducedMotion) {
        androidx.compose.foundation.layout.Box(
            modifier = modifier.background(color = baseColor, shape = shape),
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val translate by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = Motion.SHIMMER_LOOP_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeletonShimmerTranslate",
    )

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translate - 500f, 0f),
        end = Offset(translate + 500f, 0f),
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier.background(brush = brush, shape = shape),
    )
}
