package com.lifevault.app.core.designsystem.icon

import androidx.annotation.DrawableRes
import com.lifevault.app.R

/**
 * Maps a category's `iconKey` (Section 5.3: "Material Symbol name, e.g. 'badge'" — the
 * raw string stored in [com.lifevault.app.core.database.entity.CategoryEntity]) to its
 * drawable resource. Built as an explicit map (not `Resources.getIdentifier`, which is
 * slow and unsafe under resource shrinking) covering every icon Section 4.4 names for
 * built-in categories and the custom-category picker.
 */
private val ICON_KEY_TO_RES: Map<String, Int> = mapOf(
    "badge" to R.drawable.ic_badge,
    "flight" to R.drawable.ic_flight,
    "directions_car" to R.drawable.ic_directions_car,
    "health_and_safety" to R.drawable.ic_health_and_safety,
    "medical_information" to R.drawable.ic_medical_information,
    "account_balance" to R.drawable.ic_account_balance,
    "home_work" to R.drawable.ic_home_work,
    "receipt_long" to R.drawable.ic_receipt_long,
    "autorenew" to R.drawable.ic_autorenew,
    "school" to R.drawable.ic_school,
    "folder" to R.drawable.ic_folder,
    "credit_card" to R.drawable.ic_credit_card,
    "pets" to R.drawable.ic_pets,
    "work" to R.drawable.ic_work,
    "sports_esports" to R.drawable.ic_sports_esports,
    "smartphone" to R.drawable.ic_smartphone,
    "laptop" to R.drawable.ic_laptop_mac,
    "bolt" to R.drawable.ic_bolt,
    "water_drop" to R.drawable.ic_water_drop,
    "wifi" to R.drawable.ic_wifi,
    "family_restroom" to R.drawable.ic_family_restroom,
    "child_care" to R.drawable.ic_child_care,
    "elderly" to R.drawable.ic_elderly,
    "gavel" to R.drawable.ic_gavel,
    "description" to R.drawable.ic_description,
    "local_hospital" to R.drawable.ic_local_hospital,
    "vaccines" to R.drawable.ic_vaccines,
    "two_wheeler" to R.drawable.ic_two_wheeler,
    "apartment" to R.drawable.ic_apartment,
    "savings" to R.drawable.ic_savings,
    "payments" to R.drawable.ic_payments,
    "shield" to R.drawable.ic_shield,
    "key" to R.drawable.ic_key,
    "card_membership" to R.drawable.ic_card_membership,
    "star" to R.drawable.ic_star,
)

/** Falls back to the "Other" folder icon for an unrecognised/corrupted key. */
@DrawableRes
fun categoryIconRes(iconKey: String): Int = ICON_KEY_TO_RES[iconKey] ?: LifeVaultIcons.Category.Other
