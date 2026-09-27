package com.lifevault.app.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.database.entity.ReminderEntity
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.spacing.Spacing

/** S14 (Section 3.3): "Remind me" bottom sheet. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReminderEditorSheet(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Text(text = "Remind me", style = MaterialTheme.typography.titleLarge)

        for (reminder in state.reminders) {
            ReminderRow(reminder = reminder, onDelete = { viewModel.delete(reminder) })
        }

        Text(text = "Add reminder", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            for (offset in state.presetOffsets) {
                val alreadyAdded = state.reminders.any { it.offsetDays == offset }
                FilterChip(
                    selected = alreadyAdded,
                    enabled = !alreadyAdded && state.canAddMore,
                    onClick = { viewModel.addPreset(offset) },
                    label = { Text(reminderOffsetLabel(offset)) },
                )
            }
        }
        if (!state.canAddMore) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(LifeVaultIcons.Core.Lock), contentDescription = null)
                Text(
                    text = "Free plan allows up to 2 reminders — upgrade for up to 6",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = Spacing.sm),
                )
            }
        }
        if (state.duplicateOffsetError != null) {
            Text(
                text = "That reminder already exists",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}

@Composable
private fun ReminderRow(reminder: ReminderEntity, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = reminderOffsetLabel(reminder.offsetDays), style = MaterialTheme.typography.bodyLarge)
        IconButton(onClick = onDelete) {
            Icon(painterResource(LifeVaultIcons.Core.Trash), contentDescription = "Delete reminder")
        }
    }
}
