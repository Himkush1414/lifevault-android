package com.lifevault.app.feature.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.spacing.Spacing

/** S11 (Section 3.3): the add-document bottom sheet, gated on the Free document limit. */
@Composable
fun AddSourceSheet(
    onSelect: (CaptureSource) -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddSourceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(text = "Add document", style = MaterialTheme.typography.titleLarge)

        if (state.limitReached) {
            Text(
                text = "You've reached the free plan's 25-document limit.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = Spacing.md),
            )
            Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) {
                Text("Upgrade to Pro")
            }
        } else {
            SourceRow(
                icon = LifeVaultIcons.Core.Scan,
                title = "Scan with camera",
                subtitle = "Auto-detects edges, crops and cleans up",
                onClick = { onSelect(CaptureSource.SCAN) },
            )
            SourceRow(
                icon = LifeVaultIcons.Core.Photos,
                title = "Import from photos",
                subtitle = "Pick one or more images",
                onClick = { onSelect(CaptureSource.PHOTOS) },
            )
            SourceRow(
                icon = LifeVaultIcons.Core.Pdf,
                title = "Import PDF",
                subtitle = "From Files, Downloads or Drive",
                onClick = { onSelect(CaptureSource.PDF) },
            )
            SourceRow(
                icon = LifeVaultIcons.Core.Manual,
                title = "Enter manually",
                subtitle = "No file, just details and reminders",
                onClick = { onSelect(CaptureSource.MANUAL) },
            )
        }
    }
}

@Composable
private fun SourceRow(icon: Int, title: String, subtitle: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null)
        }
        Column(modifier = Modifier.padding(start = Spacing.md)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
