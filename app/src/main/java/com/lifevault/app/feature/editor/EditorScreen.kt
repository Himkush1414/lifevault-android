package com.lifevault.app.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.lifevault.app.core.database.entity.CategoryEntity
import com.lifevault.app.core.designsystem.component.ConfirmDialog
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.spacing.Spacing
import java.time.LocalDate
import java.time.ZoneOffset

/** S13 (Section 3.3): add/edit document form. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onSaved: (documentId: String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showDiscardConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedDocumentId) {
        state.savedDocumentId?.let(onSaved)
    }

    fun requestClose() {
        if (state.isDirty) showDiscardConfirm = true else onClose()
    }

    Column(modifier = modifier) {
        TopAppBar(
            title = { Text(if (state.isEditingExisting) "Edit document" else "New document") },
            navigationIcon = {
                IconButton(onClick = ::requestClose) {
                    Icon(painterResource(LifeVaultIcons.Core.Close), contentDescription = "Close")
                }
            },
            actions = {
                TextButton(onClick = viewModel::save, enabled = state.canSave && !state.isSaving) {
                    Text("Save")
                }
            },
        )

        if (!state.isLoading) {
            EditorForm(state = state, viewModel = viewModel, modifier = Modifier.verticalScroll(rememberScrollState()))
        }
    }

    if (showDiscardConfirm) {
        ConfirmDialog(
            title = "Discard changes?",
            text = "Your edits haven't been saved.",
            confirmText = "Discard",
            isDestructive = true,
            onConfirm = { showDiscardConfirm = false; onClose() },
            onDismiss = { showDiscardConfirm = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorForm(state: EditorUiState, viewModel: EditorViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxl),
    ) {
        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::onTitleChange,
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        CategoryDropdown(
            categories = state.categories,
            selectedId = state.categoryId,
            onSelect = viewModel::onCategoryChange,
        )

        OutlinedTextField(
            value = state.documentNumber,
            onValueChange = viewModel::onDocumentNumberChange,
            label = { Text("Document number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.issuer,
            onValueChange = viewModel::onIssuerChange,
            label = { Text("Issuer / provider") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            DateField(
                label = "Issue date",
                date = state.issueDate,
                onDateSelected = viewModel::onIssueDateChange,
                modifier = Modifier.weight(1f),
            )
            DateField(
                label = "Expiry date",
                date = state.expiryDate,
                onDateSelected = viewModel::onExpiryDateChange,
                enabled = !state.noExpiry,
                modifier = Modifier.weight(1f),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = state.noExpiry, onCheckedChange = viewModel::onNoExpiryToggle)
            Text("No expiry", modifier = Modifier.padding(start = Spacing.sm))
        }

        if (state.expiryAlreadyPastWarning) {
            Text(
                text = "This document is already expired.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        if (state.expiryDate != null && state.defaultReminderOffsets.isNotEmpty() && !state.isEditingExisting) {
            Text(
                text = "Reminders: " + state.defaultReminderOffsets.joinToString(", ") { "$it days" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = state.notes,
            onValueChange = viewModel::onNotesChange,
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<CategoryEntity>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (category in categories) {
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = { onSelect(category.id); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = date?.toString().orEmpty(),
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        label = { Text(label) },
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = { showPicker = true }, enabled = enabled) {
                Icon(painterResource(LifeVaultIcons.Core.Deadlines), contentDescription = "Pick date")
            }
        },
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = pickerState.selectedDateMillis
                    onDateSelected(millis?.let { LocalDate.ofEpochDay(it / 86_400_000L) })
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            androidx.compose.material3.DatePicker(state = pickerState)
        }
    }
}
