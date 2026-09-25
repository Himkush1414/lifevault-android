package com.lifevault.app.core.designsystem.icon

import androidx.annotation.DrawableRes
import com.lifevault.app.R

/**
 * Material Symbols Rounded, weight 400 / grade 0 / optical size 24 / fill 0 (Section 4.4).
 * The only permitted icon set in the app — no other icon set may be mixed in.
 */
object LifeVaultIcons {

    /** Core chrome icon map (Section 4.4). */
    object Core {
        @DrawableRes val Home = R.drawable.ic_home
        @DrawableRes val Documents = R.drawable.ic_folder_open
        @DrawableRes val Deadlines = R.drawable.ic_event
        @DrawableRes val Settings = R.drawable.ic_settings
        @DrawableRes val Add = R.drawable.ic_add
        @DrawableRes val Scan = R.drawable.ic_document_scanner
        @DrawableRes val Photos = R.drawable.ic_photo_library
        @DrawableRes val Pdf = R.drawable.ic_picture_as_pdf
        @DrawableRes val Manual = R.drawable.ic_edit_note
        @DrawableRes val Search = R.drawable.ic_search
        @DrawableRes val Lock = R.drawable.ic_lock
        @DrawableRes val Fingerprint = R.drawable.ic_fingerprint
        @DrawableRes val Backup = R.drawable.ic_backup
        @DrawableRes val Restore = R.drawable.ic_settings_backup_restore
        @DrawableRes val Reminders = R.drawable.ic_notifications
        @DrawableRes val Expired = R.drawable.ic_error
        @DrawableRes val Critical = R.drawable.ic_schedule
        @DrawableRes val Due = R.drawable.ic_event_upcoming
        @DrawableRes val Valid = R.drawable.ic_check_circle
        @DrawableRes val NoExpiry = R.drawable.ic_all_inclusive
        @DrawableRes val Share = R.drawable.ic_share
        @DrawableRes val Trash = R.drawable.ic_delete
        @DrawableRes val Pro = R.drawable.ic_workspace_premium
        @DrawableRes val Backspace = R.drawable.ic_backspace
    }

    /** Built-in category icons (Section 4.4). */
    object Category {
        @DrawableRes val Identity = R.drawable.ic_badge
        @DrawableRes val Travel = R.drawable.ic_flight
        @DrawableRes val Vehicle = R.drawable.ic_directions_car
        @DrawableRes val Insurance = R.drawable.ic_health_and_safety
        @DrawableRes val Medical = R.drawable.ic_medical_information
        @DrawableRes val FinanceAndTax = R.drawable.ic_account_balance
        @DrawableRes val Property = R.drawable.ic_home_work
        @DrawableRes val WarrantyAndReceipts = R.drawable.ic_receipt_long
        @DrawableRes val Subscriptions = R.drawable.ic_autorenew
        @DrawableRes val Education = R.drawable.ic_school
        @DrawableRes val Other = R.drawable.ic_folder
    }

    /** The 24 additional curated glyphs offered by the custom-category icon picker (S08). */
    object CategoryPickerExtra {
        @DrawableRes val CreditCard = R.drawable.ic_credit_card
        @DrawableRes val Pets = R.drawable.ic_pets
        @DrawableRes val Work = R.drawable.ic_work
        @DrawableRes val SportsEsports = R.drawable.ic_sports_esports
        @DrawableRes val Smartphone = R.drawable.ic_smartphone
        @DrawableRes val Laptop = R.drawable.ic_laptop_mac // Material Symbols has no plain "laptop"
        @DrawableRes val Bolt = R.drawable.ic_bolt
        @DrawableRes val WaterDrop = R.drawable.ic_water_drop
        @DrawableRes val Wifi = R.drawable.ic_wifi
        @DrawableRes val FamilyRestroom = R.drawable.ic_family_restroom
        @DrawableRes val ChildCare = R.drawable.ic_child_care
        @DrawableRes val Elderly = R.drawable.ic_elderly
        @DrawableRes val Gavel = R.drawable.ic_gavel
        @DrawableRes val Description = R.drawable.ic_description
        @DrawableRes val LocalHospital = R.drawable.ic_local_hospital
        @DrawableRes val Vaccines = R.drawable.ic_vaccines
        @DrawableRes val TwoWheeler = R.drawable.ic_two_wheeler
        @DrawableRes val Apartment = R.drawable.ic_apartment
        @DrawableRes val Savings = R.drawable.ic_savings
        @DrawableRes val Payments = R.drawable.ic_payments
        @DrawableRes val Shield = R.drawable.ic_shield
        @DrawableRes val Key = R.drawable.ic_key
        @DrawableRes val CardMembership = R.drawable.ic_card_membership
        @DrawableRes val Star = R.drawable.ic_star
    }
}
