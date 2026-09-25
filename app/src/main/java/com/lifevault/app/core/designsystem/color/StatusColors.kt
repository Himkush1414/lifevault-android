package com.lifevault.app.core.designsystem.color

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * One content/container pair per document status (Section 4.1.3). Exposed
 * through [LocalStatusColors] rather than the Material [androidx.compose.material3.ColorScheme]
 * because these colours carry semantic meaning and must never be replaced by
 * dynamic colour (Section 4.1.5).
 */
data class StatusColor(
    val content: Color,
    val container: Color,
)

data class StatusColors(
    val expired: StatusColor,
    val critical: StatusColor,
    val dueSoon: StatusColor,
    val valid: StatusColor,
    val noExpiry: StatusColor,
)

internal val LightStatusColors = StatusColors(
    expired = StatusColor(content = Color(0xFFB3261E), container = Color(0xFFFCE8E6)),
    critical = StatusColor(content = Color(0xFFB45309), container = Color(0xFFFEF0E1)),
    dueSoon = StatusColor(content = Color(0xFF8A6100), container = Color(0xFFFEF7E0)),
    valid = StatusColor(content = Color(0xFF146C2E), container = Color(0xFFE6F4EA)),
    noExpiry = StatusColor(content = Color(0xFF5F6368), container = Color(0xFFF1F3F4)),
)

internal val DarkStatusColors = StatusColors(
    expired = StatusColor(content = Color(0xFFF2B8B5), container = Color(0xFF601410)),
    critical = StatusColor(content = Color(0xFFFFB870), container = Color(0xFF5A2E00)),
    dueSoon = StatusColor(content = Color(0xFFFDD663), container = Color(0xFF4A3800)),
    valid = StatusColor(content = Color(0xFF81C995), container = Color(0xFF0D3B1C)),
    noExpiry = StatusColor(content = Color(0xFFBDC1C6), container = Color(0xFF303134)),
)

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }
