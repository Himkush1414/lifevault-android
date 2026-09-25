package com.lifevault.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lifevault.app.core.designsystem.spacing.Spacing

/**
 * The subtle free-tier usage row (Section 3.3 S07): "22 of 25 free documents used".
 * `labelMedium`, low-emphasis — this is a nudge, not a paywall interruption.
 */
@Composable
fun LimitBanner(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String = "Upgrade",
    onActionClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onActionClick) {
            Text(text = actionText, style = MaterialTheme.typography.labelMedium)
        }
    }
}
