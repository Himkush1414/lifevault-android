package com.lifevault.app.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons

private val KeypadRows = listOf(
    listOf(1, 2, 3),
    listOf(4, 5, 6),
    listOf(7, 8, 9),
)

/**
 * The 3x4 numeric keypad used for PIN entry (Section 3.3 S04/S05): no system keyboard,
 * so keyboard apps can never log the PIN. Each key is 72dp with a `titleLarge` digit and
 * an accessibility label ("Digit 5", Section 4.7).
 */
@Composable
fun NumericKeypad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (row in KeypadRows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (digit in row) {
                    KeypadKey(
                        contentDescription = "Digit $digit",
                        onClick = { onDigit(digit) },
                    ) {
                        Text(text = digit.toString(), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(72.dp))
            KeypadKey(contentDescription = "Digit 0", onClick = { onDigit(0) }) {
                Text(text = "0", style = MaterialTheme.typography.titleLarge)
            }
            KeypadKey(contentDescription = "Delete last digit", onClick = onBackspace) {
                Icon(painter = painterResource(LifeVaultIcons.Core.Backspace), contentDescription = null)
            }
        }
    }
}

@Composable
private fun KeypadKey(
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 36.dp),
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
