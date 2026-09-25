package com.lifevault.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifevault.app.core.designsystem.color.CategoryColorPair
import com.lifevault.app.core.designsystem.color.StatusColor
import com.lifevault.app.core.designsystem.spacing.Dimens
import com.lifevault.app.core.designsystem.spacing.Spacing

/**
 * One row in a document list (Section 4.8, min height 72dp). Merged into a single
 * TalkBack node reading e.g. "Passport, Identity, expires in 12 days" (Section 4.7) —
 * the status pill's own text carries that into the merged description, so callers
 * don't need to duplicate it.
 *
 * @param thumbnailPainter When non-null, a 48x48dp 8dp-radius image thumbnail replaces
 * the category avatar circle (Section 4.8).
 */
@Composable
fun DocumentRow(
    title: String,
    supportingText: String,
    statusText: String,
    @DrawableRes statusIcon: Int,
    statusColors: StatusColor,
    @DrawableRes categoryIcon: Int,
    categoryColors: CategoryColorPair,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailPainter: Painter? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = Dimens.listRowHeightTwoLine)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg)
            .clearAndSetSemantics {
                contentDescription = "$title, $supportingText, $statusText"
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (thumbnailPainter != null) {
            Image(
                painter = thumbnailPainter,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            CategoryAvatar(icon = categoryIcon, colors = categoryColors)
        }

        Spacer(Modifier.width(Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(Spacing.md))

        StatusPill(text = statusText, icon = statusIcon, colors = statusColors)
    }
}
