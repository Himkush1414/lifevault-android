package com.lifevault.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lifevault.app.core.designsystem.color.StatusColor
import com.lifevault.app.core.designsystem.spacing.Spacing

/**
 * A document status pill (Section 4.8): "12 days" / "Expired" / "Valid" / "No expiry".
 * Status is always communicated with icon + text together, never colour alone
 * (Section 4.1.3 colour-blind safety rule).
 *
 * [text] should already be resolved to the caller's preferred form — e.g. "Today",
 * "Expired 3 days ago" on detail screens vs. "Expired" in list rows (Section 4.8).
 */
@Composable
fun StatusPill(
    text: String,
    @DrawableRes icon: Int,
    colors: StatusColor,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalContentColor provides colors.content) {
        Row(
            modifier = modifier
                .height(24.dp)
                .background(color = colors.container, shape = RoundedCornerShape(50))
                .padding(horizontal = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(Spacing.xxs))
            Text(text = text, style = MaterialTheme.typography.labelMedium)
        }
    }
}
