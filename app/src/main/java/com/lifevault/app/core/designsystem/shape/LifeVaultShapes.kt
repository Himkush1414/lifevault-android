package com.lifevault.app.core.designsystem.shape

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Corner radius scale (Section 4.3). */
object LifeVaultShapes {
    val extraSmall = RoundedCornerShape(4.dp) // badges
    val small = RoundedCornerShape(8.dp) // chips, text fields
    val medium = RoundedCornerShape(12.dp) // thumbnails, menus
    val large = RoundedCornerShape(16.dp) // cards
    val extraLarge = RoundedCornerShape(28.dp) // hero card, bottom sheet top corners, dialogs
    val full = CircleShape // pills

    /** Material 3's shape slots, mapped onto the scale above. */
    val materialShapes = Shapes(
        extraSmall = extraSmall,
        small = small,
        medium = medium,
        large = large,
        extraLarge = extraLarge,
    )
}
