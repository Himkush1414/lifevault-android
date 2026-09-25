package com.lifevault.app.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifevault.app.core.designsystem.color.CategoryColorPair

/**
 * A category icon on a 40dp coloured circle (Section 4.1.4 / 4.4). [DocumentRow] swaps
 * this for a 48x48dp thumbnail when the document has an image (Section 4.8).
 */
@Composable
fun CategoryAvatar(
    @DrawableRes icon: Int,
    colors: CategoryColorPair,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = colors.container, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.icon,
            modifier = Modifier.size(size * 0.6f),
        )
    }
}
