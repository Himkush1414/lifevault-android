package com.lifevault.app.core.designsystem.spacing

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The 4dp-base spacing scale (Section 4.3). Feature code must use these, never raw `.dp`. */
object Spacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val huge: Dp = 40.dp
    val giant: Dp = 48.dp
    val max: Dp = 64.dp
}

/** Named contextual values from the Section 4.3 table, for readability at call sites. */
object Dimens {
    // Screen horizontal margin by width class (compact/medium/expanded).
    val screenMarginCompact: Dp = Spacing.lg // < 600dp
    val screenMarginMedium: Dp = Spacing.xxl // 600–839dp
    val screenMarginExpanded: Dp = Spacing.xxxl // ≥ 840dp

    val maxContentWidth: Dp = 840.dp

    val sectionGap: Dp = Spacing.xxl
    val cardListGap: Dp = Spacing.md
    val cardPadding: Dp = Spacing.lg
    val heroCardPadding: Dp = Spacing.xl

    val listRowHeightTwoLine: Dp = 72.dp
    val listRowHeightOneLine: Dp = 56.dp

    val minTouchTarget: Dp = 48.dp

    val iconTextGapRow: Dp = Spacing.md
    val iconTextGapButton: Dp = Spacing.sm
}
