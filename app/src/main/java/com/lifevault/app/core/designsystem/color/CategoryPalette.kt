package com.lifevault.app.core.designsystem.color

import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

/** One of the 12 category hues (Section 4.1.4), each with a light and dark icon/container pair. */
enum class CategoryColorKey {
    Blue, Indigo, Purple, Pink, Red, Orange, Amber, Green, Teal, Cyan, Brown, Grey,
}

data class CategoryColorPair(val icon: Color, val container: Color)

private val LightCategoryColors: Map<CategoryColorKey, CategoryColorPair> = mapOf(
    CategoryColorKey.Blue to CategoryColorPair(Color(0xFF1A73E8), Color(0xFFE8F0FE)),
    CategoryColorKey.Indigo to CategoryColorPair(Color(0xFF4B53BC), Color(0xFFE8EAFB)),
    CategoryColorKey.Purple to CategoryColorPair(Color(0xFF8430CE), Color(0xFFF3E8FD)),
    CategoryColorKey.Pink to CategoryColorPair(Color(0xFFC2185B), Color(0xFFFCE4EC)),
    CategoryColorKey.Red to CategoryColorPair(Color(0xFFC5221F), Color(0xFFFCE8E6)),
    CategoryColorKey.Orange to CategoryColorPair(Color(0xFFC25400), Color(0xFFFEEFE3)),
    CategoryColorKey.Amber to CategoryColorPair(Color(0xFF8F6200), Color(0xFFFEF7E0)),
    CategoryColorKey.Green to CategoryColorPair(Color(0xFF137333), Color(0xFFE6F4EA)),
    CategoryColorKey.Teal to CategoryColorPair(Color(0xFF00796B), Color(0xFFE0F2F1)),
    CategoryColorKey.Cyan to CategoryColorPair(Color(0xFF007B83), Color(0xFFE4F7FB)),
    CategoryColorKey.Brown to CategoryColorPair(Color(0xFF795548), Color(0xFFEFEBE9)),
    CategoryColorKey.Grey to CategoryColorPair(Color(0xFF5F6368), Color(0xFFF1F3F4)),
)

private val DarkCategoryColors: Map<CategoryColorKey, CategoryColorPair> = mapOf(
    CategoryColorKey.Blue to CategoryColorPair(Color(0xFF8AB4F8), Color(0xFF1B3A66)),
    CategoryColorKey.Indigo to CategoryColorPair(Color(0xFFAEB4F5), Color(0xFF2A2F6B)),
    CategoryColorKey.Purple to CategoryColorPair(Color(0xFFD7AEFB), Color(0xFF4A2270)),
    CategoryColorKey.Pink to CategoryColorPair(Color(0xFFF48FB1), Color(0xFF6A1238)),
    CategoryColorKey.Red to CategoryColorPair(Color(0xFFF28B82), Color(0xFF6B1714)),
    CategoryColorKey.Orange to CategoryColorPair(Color(0xFFFCAD70), Color(0xFF5C2B00)),
    CategoryColorKey.Amber to CategoryColorPair(Color(0xFFFDD663), Color(0xFF4A3800)),
    CategoryColorKey.Green to CategoryColorPair(Color(0xFF81C995), Color(0xFF0D3B1C)),
    CategoryColorKey.Teal to CategoryColorPair(Color(0xFF80CBC4), Color(0xFF003D36)),
    CategoryColorKey.Cyan to CategoryColorPair(Color(0xFF78D9EC), Color(0xFF003A40)),
    CategoryColorKey.Brown to CategoryColorPair(Color(0xFFBCAAA4), Color(0xFF3E2723)),
    CategoryColorKey.Grey to CategoryColorPair(Color(0xFFBDC1C6), Color(0xFF303134)),
)

/** Resolves this hue's icon/container pair for the current [isSystemInDarkTheme] state. */
@Composable
fun CategoryColorKey.colorPair(darkTheme: Boolean = isSystemInDarkTheme()): CategoryColorPair =
    (if (darkTheme) DarkCategoryColors else LightCategoryColors).getValue(this)
