package com.lifevault.app.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.lifevault.app.core.designsystem.component.LimitBanner
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.spacing.Spacing
import com.lifevault.app.core.domain.feature.FeatureGate

/** S12 (Section 3.3): reorder/rotate/delete captured pages before the save pipeline runs. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureReviewScreen(
    onSaved: (documentId: String) -> Unit,
    onCancel: () -> Unit,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CaptureReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.savedDocumentId) {
        state.savedDocumentId?.let(onSaved)
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Review") },
            navigationIcon = {
                IconButton(onClick = { viewModel.cancel(); onCancel() }) {
                    Icon(painterResource(LifeVaultIcons.Core.Close), contentDescription = "Cancel")
                }
            },
        )

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            }
            return@Column
        }

        if (state.overLimit) {
            val limit = if (state.isPro) {
                FeatureGate.PRO_SOFT_CAP_FILES_PER_DOCUMENT
            } else {
                FeatureGate.FREE_MAX_FILES_PER_DOCUMENT
            }
            LimitBanner(
                text = "Remove pages down to $limit files to continue.",
                actionText = "Upgrade",
                onActionClick = onUpgrade,
            )
        }

        val pdf = state.pdf
        if (pdf != null) {
            PdfSummaryRow(pdf)
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.pages, key = { it.id }) { page ->
                    PageRow(
                        page = page,
                        onRotate = { viewModel.rotate(page.id) },
                        onDelete = { viewModel.delete(page.id) },
                        onMoveUp = {
                            val index = state.pages.indexOfFirst { it.id == page.id }
                            if (index > 0) viewModel.reorder(index, index - 1)
                        },
                        onMoveDown = {
                            val index = state.pages.indexOfFirst { it.id == page.id }
                            if (index < state.pages.lastIndex) viewModel.reorder(index, index + 1)
                        },
                    )
                }
            }
        }

        Button(
            onClick = viewModel::save,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Save (${state.fileCount})")
            }
        }
    }
}

@Composable
private fun PdfSummaryRow(pdf: PdfSummary, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = pdf.thumbnailFile,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(72.dp),
        )
        Column(modifier = Modifier.padding(start = Spacing.md)) {
            Text(pdf.file.name, style = MaterialTheme.typography.titleSmall)
            Text(
                "${pdf.pageCount} page${if (pdf.pageCount == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PageRow(
    page: ReviewPage,
    onRotate: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AsyncImage(
            model = page.file,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp).rotate(page.rotationDegrees.toFloat()),
        )
        Row {
            TextButton(onClick = onMoveUp) { Text("↑") }
            TextButton(onClick = onMoveDown) { Text("↓") }
            IconButton(onClick = onRotate) {
                Icon(painterResource(LifeVaultIcons.Core.Rotate), contentDescription = "Rotate")
            }
            IconButton(onClick = onDelete) {
                Icon(painterResource(LifeVaultIcons.Core.Trash), contentDescription = "Delete page")
            }
        }
    }
}
