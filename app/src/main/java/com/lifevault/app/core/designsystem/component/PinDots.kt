package com.lifevault.app.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp

/**
 * The 6-dot PIN entry indicator (Section 3.3 S04/S05, 12dp dots with 16dp gaps).
 * Announces "N of [totalDigits] digits entered" as one merged node (Section 4.7).
 */
@Composable
fun PinDots(
    digitsEntered: Int,
    modifier: Modifier = Modifier,
    totalDigits: Int = 6,
) {
    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "$digitsEntered of $totalDigits digits entered"
        },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(totalDigits) { index ->
            val filled = index < digitsEntered
            val color = if (filled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
