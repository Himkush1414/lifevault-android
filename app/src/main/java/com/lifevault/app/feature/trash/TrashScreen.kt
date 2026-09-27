package com.lifevault.app.feature.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.database.relation.DocumentWithCategory
import com.lifevault.app.core.designsystem.component.ConfirmDialog
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.spacing.Spacing
import java.time.Duration
import java.time.Instant

/** S23 (Section 3.3): Trash. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrashScreen(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: TrashViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(if (state.isSelectionMode) "${state.selectedIds.size} selected" else "Trash")
            },
            navigationIcon = {
                IconButton(onClick = { if (state.isSelectionMode) viewModel.clearSelection() else onBack() }) {
                    Icon(painterResource(LifeVaultIcons.Core.ArrowBack), contentDescription = "Back")
                }
            },
            actions = {
                if (state.isSelectionMode) {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(painterResource(LifeVaultIcons.Core.Trash), contentDescription = "Delete permanently")
                    }
                }
            },
        )

        Text(
            text = "Items are permanently deleted after 30 days.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.lg),
        )

        LazyColumn {
            items(state.documents, key = { it.document.id }) { item ->
                TrashRow(
                    item = item,
                    selected = item.document.id in state.selectedIds,
                    selectionMode = state.isSelectionMode,
                    onClick = {
                        if (state.isSelectionMode) viewModel.toggleSelection(item.document.id)
                    },
                    onLongClick = { viewModel.toggleSelection(item.document.id) },
                    onRestore = { viewModel.restore(item.document.id) },
                )
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete permanently?",
            text = "This cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = { showDeleteConfirm = false; viewModel.permanentlyDeleteSelected() },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrashRow(
    item: DocumentWithCategory,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectionMode) {
            Checkbox(checked = selected, onCheckedChange = { onClick() })
        }
        Column(modifier = Modifier.weight(1f).padding(start = if (selectionMode) Spacing.sm else 0.dp)) {
            Text(text = item.document.title, style = MaterialTheme.typography.titleMedium)
            item.document.deletedAt?.let {
                Text(
                    text = "Deleted ${daysAgo(it)} days ago",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!selectionMode) {
            TextButton(onClick = onRestore) {
                Text("Restore")
            }
        }
    }
}

private fun daysAgo(instant: Instant): Long = Duration.between(instant, Instant.now()).toDays()
