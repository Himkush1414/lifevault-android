package com.lifevault.app.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.designsystem.color.categoryColorKeyOf
import com.lifevault.app.core.designsystem.color.colorPair
import com.lifevault.app.core.designsystem.component.CategoryAvatar
import com.lifevault.app.core.designsystem.component.ConfirmDialog
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.icon.categoryIconRes
import com.lifevault.app.core.designsystem.spacing.Spacing
import com.lifevault.app.core.domain.status.DocumentStatus
import com.lifevault.app.core.domain.status.statusPillDetailLabel

/** S09 (Section 3.3): document detail. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    onBack: () -> Unit,
    onEdit: (documentId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DocumentDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showTrashConfirm by remember { mutableStateOf(false) }
    var showRenewedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.movedToTrash) {
        if (state.movedToTrash) onBack()
    }

    val details = state.documentWithDetails

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(details?.document?.title.orEmpty()) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painterResource(LifeVaultIcons.Core.ArrowBack), contentDescription = "Back")
                }
            },
            actions = {
                details?.let {
                    IconButton(onClick = { onEdit(it.document.id) }) {
                        Icon(painterResource(LifeVaultIcons.Core.Documents), contentDescription = "Edit")
                    }
                    IconButton(onClick = { showTrashConfirm = true }) {
                        Icon(painterResource(LifeVaultIcons.Core.Trash), contentDescription = "Move to trash")
                    }
                }
            },
        )

        if (details != null) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxl),
            ) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    CategoryAvatar(
                        icon = categoryIconRes(details.category.iconKey),
                        colors = categoryColorKeyOf(details.category.colorKey).colorPair(),
                    )
                    Text(
                        text = details.category.name,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = Spacing.sm),
                    )
                }

                Text(text = details.document.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = statusPillDetailLabel(details.document.expiryDate, state.today),
                    style = MaterialTheme.typography.bodyLarge,
                    color = statusColor(state.status),
                )

                KeyFactsCard(details)

                Text(
                    text = "Reminders: ${details.reminders.size}",
                    style = MaterialTheme.typography.titleSmall,
                )

                if (!details.document.notes.isNullOrBlank()) {
                    Text(text = details.document.notes, style = MaterialTheme.typography.bodyLarge)
                }

                if (state.status == DocumentStatus.EXPIRED || state.status == DocumentStatus.CRITICAL) {
                    Button(onClick = { showRenewedDialog = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Mark as renewed")
                    }
                }
            }
        }
    }

    if (showTrashConfirm) {
        ConfirmDialog(
            title = "Move to trash?",
            text = "This document can be restored within 30 days.",
            confirmText = "Move to trash",
            isDestructive = true,
            onConfirm = { showTrashConfirm = false; viewModel.moveToTrash() },
            onDismiss = { showTrashConfirm = false },
        )
    }

    if (showRenewedDialog) {
        val suggested = viewModel.suggestedRenewalDate() ?: state.today.plusYears(1)
        ConfirmDialog(
            title = "Mark as renewed?",
            text = "New expiry date: $suggested",
            confirmText = "Confirm",
            onConfirm = { showRenewedDialog = false; viewModel.markAsRenewed(suggested) },
            onDismiss = { showRenewedDialog = false },
        )
    }
}

@Composable
private fun KeyFactsCard(details: com.lifevault.app.core.database.relation.DocumentWithDetails) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        details.document.documentNumber?.let { FactRow("Document number", it) }
        details.document.issuer?.let { FactRow("Issuer", it) }
        details.document.issueDate?.let { FactRow("Issued on", it.toString()) }
        details.document.expiryDate?.let { FactRow("Expires on", it.toString()) }
    }
}

@Composable
private fun FactRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun statusColor(status: DocumentStatus) = when (status) {
    DocumentStatus.EXPIRED -> MaterialTheme.colorScheme.error
    DocumentStatus.CRITICAL, DocumentStatus.DUE_SOON -> MaterialTheme.colorScheme.tertiary
    DocumentStatus.VALID -> MaterialTheme.colorScheme.primary
    DocumentStatus.NO_EXPIRY -> MaterialTheme.colorScheme.onSurfaceVariant
}
