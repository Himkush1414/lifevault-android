package com.lifevault.app.designcatalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lifevault.app.core.designsystem.color.CategoryColorKey
import com.lifevault.app.core.designsystem.color.LifeVaultTheme
import com.lifevault.app.core.designsystem.color.LocalStatusColors
import com.lifevault.app.core.designsystem.color.StatusColor
import com.lifevault.app.core.designsystem.color.colorPair
import com.lifevault.app.core.designsystem.component.CategoryAvatar
import com.lifevault.app.core.designsystem.component.ConfirmDialog
import com.lifevault.app.core.designsystem.component.DocumentRow
import com.lifevault.app.core.designsystem.component.EmptyState
import com.lifevault.app.core.designsystem.component.LimitBanner
import com.lifevault.app.core.designsystem.component.NumericKeypad
import com.lifevault.app.core.designsystem.component.PinDots
import com.lifevault.app.core.designsystem.component.SectionHeader
import com.lifevault.app.core.designsystem.component.SkeletonShimmer
import com.lifevault.app.core.designsystem.component.StatusPill
import com.lifevault.app.core.designsystem.icon.LifeVaultIcons
import com.lifevault.app.core.designsystem.spacing.Spacing

private val CoreIcons: List<Pair<String, Int>> = listOf(
    "Home" to LifeVaultIcons.Core.Home,
    "Documents" to LifeVaultIcons.Core.Documents,
    "Deadlines" to LifeVaultIcons.Core.Deadlines,
    "Settings" to LifeVaultIcons.Core.Settings,
    "Add" to LifeVaultIcons.Core.Add,
    "Scan" to LifeVaultIcons.Core.Scan,
    "Photos" to LifeVaultIcons.Core.Photos,
    "Pdf" to LifeVaultIcons.Core.Pdf,
    "Manual" to LifeVaultIcons.Core.Manual,
    "Search" to LifeVaultIcons.Core.Search,
    "Lock" to LifeVaultIcons.Core.Lock,
    "Fingerprint" to LifeVaultIcons.Core.Fingerprint,
    "Backup" to LifeVaultIcons.Core.Backup,
    "Restore" to LifeVaultIcons.Core.Restore,
    "Reminders" to LifeVaultIcons.Core.Reminders,
    "Expired" to LifeVaultIcons.Core.Expired,
    "Critical" to LifeVaultIcons.Core.Critical,
    "Due" to LifeVaultIcons.Core.Due,
    "Valid" to LifeVaultIcons.Core.Valid,
    "NoExpiry" to LifeVaultIcons.Core.NoExpiry,
    "Share" to LifeVaultIcons.Core.Share,
    "Trash" to LifeVaultIcons.Core.Trash,
    "Pro" to LifeVaultIcons.Core.Pro,
    "Backspace" to LifeVaultIcons.Core.Backspace,
)

private val CategoryIcons: List<Pair<String, Int>> = listOf(
    "Identity" to LifeVaultIcons.Category.Identity,
    "Travel" to LifeVaultIcons.Category.Travel,
    "Vehicle" to LifeVaultIcons.Category.Vehicle,
    "Insurance" to LifeVaultIcons.Category.Insurance,
    "Medical" to LifeVaultIcons.Category.Medical,
    "FinanceAndTax" to LifeVaultIcons.Category.FinanceAndTax,
    "Property" to LifeVaultIcons.Category.Property,
    "WarrantyAndReceipts" to LifeVaultIcons.Category.WarrantyAndReceipts,
    "Subscriptions" to LifeVaultIcons.Category.Subscriptions,
    "Education" to LifeVaultIcons.Category.Education,
    "Other" to LifeVaultIcons.Category.Other,
)

private val CategoryPickerExtraIcons: List<Pair<String, Int>> = listOf(
    "CreditCard" to LifeVaultIcons.CategoryPickerExtra.CreditCard,
    "Pets" to LifeVaultIcons.CategoryPickerExtra.Pets,
    "Work" to LifeVaultIcons.CategoryPickerExtra.Work,
    "SportsEsports" to LifeVaultIcons.CategoryPickerExtra.SportsEsports,
    "Smartphone" to LifeVaultIcons.CategoryPickerExtra.Smartphone,
    "Laptop" to LifeVaultIcons.CategoryPickerExtra.Laptop,
    "Bolt" to LifeVaultIcons.CategoryPickerExtra.Bolt,
    "WaterDrop" to LifeVaultIcons.CategoryPickerExtra.WaterDrop,
    "Wifi" to LifeVaultIcons.CategoryPickerExtra.Wifi,
    "FamilyRestroom" to LifeVaultIcons.CategoryPickerExtra.FamilyRestroom,
    "ChildCare" to LifeVaultIcons.CategoryPickerExtra.ChildCare,
    "Elderly" to LifeVaultIcons.CategoryPickerExtra.Elderly,
    "Gavel" to LifeVaultIcons.CategoryPickerExtra.Gavel,
    "Description" to LifeVaultIcons.CategoryPickerExtra.Description,
    "LocalHospital" to LifeVaultIcons.CategoryPickerExtra.LocalHospital,
    "Vaccines" to LifeVaultIcons.CategoryPickerExtra.Vaccines,
    "TwoWheeler" to LifeVaultIcons.CategoryPickerExtra.TwoWheeler,
    "Apartment" to LifeVaultIcons.CategoryPickerExtra.Apartment,
    "Savings" to LifeVaultIcons.CategoryPickerExtra.Savings,
    "Payments" to LifeVaultIcons.CategoryPickerExtra.Payments,
    "Shield" to LifeVaultIcons.CategoryPickerExtra.Shield,
    "Key" to LifeVaultIcons.CategoryPickerExtra.Key,
    "CardMembership" to LifeVaultIcons.CategoryPickerExtra.CardMembership,
    "Star" to LifeVaultIcons.CategoryPickerExtra.Star,
)

/**
 * A debug-only living style guide (Section 12 step 2): every colour, type scale entry,
 * icon and shared component in one scrollable screen, so a design/contrast review never
 * has to go hunting through real feature screens. Never compiled into release (this file
 * lives under `src/debug/`).
 */
@Composable
fun DesignCatalogScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xxxl),
    ) {
        CatalogSection("Status colours") { StatusColorSwatches() }
        CatalogSection("Category colours") { CategoryColorSwatches() }
        CatalogSection("Typography") { TypographySamples() }
        CatalogSection("Core icons (${CoreIcons.size})") { IconGrid(CoreIcons) }
        CatalogSection("Category icons (${CategoryIcons.size})") { IconGrid(CategoryIcons) }
        CatalogSection("Category picker — extra icons (${CategoryPickerExtraIcons.size})") {
            IconGrid(CategoryPickerExtraIcons)
        }
        CatalogSection("StatusPill") { StatusPillSamples() }
        CatalogSection("CategoryAvatar") { CategoryAvatarSamples() }
        CatalogSection("DocumentRow") { DocumentRowSample() }
        CatalogSection("SectionHeader") { SectionHeader(title = "Coming up", actionText = "See all") }
        CatalogSection("EmptyState") {
            EmptyState(
                icon = LifeVaultIcons.Core.Documents,
                title = "Your vault is empty",
                description = "Add your first document — try your driving licence or an insurance policy.",
                actionText = "Scan a document",
            )
        }
        CatalogSection("PinDots") { PinDots(digitsEntered = 3) }
        CatalogSection("NumericKeypad") {
            NumericKeypad(onDigit = {}, onBackspace = {})
        }
        CatalogSection("LimitBanner") {
            LimitBanner(text = "22 of 25 free documents used")
        }
        CatalogSection("SkeletonShimmer") {
            SkeletonShimmer(modifier = Modifier.fillMaxWidth().height(72.dp))
        }
        CatalogSection("ConfirmDialog") { ConfirmDialogSample() }
    }
}

@Composable
private fun CatalogSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        content()
    }
}

@Composable
private fun StatusColorSwatches() {
    val statuses = LocalStatusColors.current
    val labeled = listOf(
        "Expired" to statuses.expired,
        "Critical" to statuses.critical,
        "Due soon" to statuses.dueSoon,
        "Valid" to statuses.valid,
        "No expiry" to statuses.noExpiry,
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        for ((label, color) in labeled) {
            ColorSwatchRow(label, color.container, color.content)
        }
    }
}

@Composable
private fun ColorSwatchRow(label: String, container: Color, content: Color) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(40.dp)
                .background(container, RoundedCornerShape(8.dp)),
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(Spacing.md))
        Text(text = label, color = content, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun CategoryColorSwatches() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(CategoryColorKey.entries.toList()) { key ->
            val pair = key.colorPair()
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(pair.container, RoundedCornerShape(8.dp)),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text(text = key.name, color = pair.icon, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun TypographySamples() {
    val typography = MaterialTheme.typography
    val samples = listOf(
        "displaySmall" to typography.displaySmall,
        "headlineLarge" to typography.headlineLarge,
        "headlineMedium" to typography.headlineMedium,
        "headlineSmall" to typography.headlineSmall,
        "titleLarge" to typography.titleLarge,
        "titleMedium" to typography.titleMedium,
        "titleSmall" to typography.titleSmall,
        "bodyLarge" to typography.bodyLarge,
        "bodyMedium" to typography.bodyMedium,
        "bodySmall" to typography.bodySmall,
        "labelLarge" to typography.labelLarge,
        "labelMedium" to typography.labelMedium,
        "labelSmall" to typography.labelSmall,
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        for ((name, style) in samples) {
            Text(text = "$name — LifeVault", style = style)
        }
    }
}

@Composable
private fun IconGrid(icons: List<Pair<String, Int>>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 72.dp),
        modifier = Modifier.height(((icons.size / 4 + 1) * 72).dp.coerceAtMost(400.dp)),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        items(icons) { (name, res) ->
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
            ) {
                Icon(painter = painterResource(res), contentDescription = name)
                Text(text = name, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun StatusPillSamples() {
    val statuses = LocalStatusColors.current
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        StatusPill("Expired", LifeVaultIcons.Core.Expired, statuses.expired)
        StatusPill("3 days", LifeVaultIcons.Core.Critical, statuses.critical)
        StatusPill("12 days", LifeVaultIcons.Core.Due, statuses.dueSoon)
        StatusPill("Valid", LifeVaultIcons.Core.Valid, statuses.valid)
        StatusPill("No expiry", LifeVaultIcons.Core.NoExpiry, statuses.noExpiry)
    }
}

@Composable
private fun CategoryAvatarSamples() {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        CategoryAvatar(LifeVaultIcons.Category.Identity, CategoryColorKey.Blue.colorPair())
        CategoryAvatar(LifeVaultIcons.Category.Vehicle, CategoryColorKey.Orange.colorPair())
        CategoryAvatar(LifeVaultIcons.Category.Insurance, CategoryColorKey.Teal.colorPair())
    }
}

@Composable
private fun DocumentRowSample() {
    val statuses = LocalStatusColors.current
    DocumentRow(
        title = "Passport",
        supportingText = "Identity · No. ••••4821",
        statusText = "12 days",
        statusIcon = LifeVaultIcons.Core.Due,
        statusColors = statuses.dueSoon,
        categoryIcon = LifeVaultIcons.Category.Identity,
        categoryColors = CategoryColorKey.Blue.colorPair(),
        onClick = {},
    )
}

@Composable
private fun ConfirmDialogSample() {
    var show by remember { mutableStateOf(false) }
    androidx.compose.material3.TextButton(onClick = { show = true }) {
        Text("Show confirm dialog")
    }
    if (show) {
        ConfirmDialog(
            title = "Move to trash?",
            text = "This document can be restored within 30 days.",
            confirmText = "Move to trash",
            isDestructive = true,
            onConfirm = { show = false },
            onDismiss = { show = false },
        )
    }
}

@Preview(name = "Light", showBackground = true, heightDp = 2400)
@Composable
private fun DesignCatalogLightPreview() {
    LifeVaultTheme(darkTheme = false) { DesignCatalogScreen() }
}

@Preview(name = "Dark", showBackground = true, heightDp = 2400, uiMode = 0x20)
@Composable
private fun DesignCatalogDarkPreview() {
    LifeVaultTheme(darkTheme = true) { DesignCatalogScreen() }
}

@Preview(name = "Light — 200% font", showBackground = true, heightDp = 3200, fontScale = 2f)
@Composable
private fun DesignCatalogLargeFontPreview() {
    LifeVaultTheme(darkTheme = false) { DesignCatalogScreen() }
}
