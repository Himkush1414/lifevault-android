package com.lifevault.app.core.util

import android.os.Build

/** Section 7.6: manufacturers whose OEM skins are known to kill background work aggressively. */
private val AGGRESSIVE_OEM_MANUFACTURERS = setOf(
    "xiaomi", "redmi", "poco", "oppo", "realme", "oneplus", "vivo", "iqoo",
    "samsung", "huawei", "honor", "tecno", "infinix", "itel", "asus",
)

/** Whether the "Keep reminders reliable" card/reliability guide should be shown. */
fun isAggressiveBatteryOem(manufacturer: String = Build.MANUFACTURER): Boolean =
    manufacturer.lowercase() in AGGRESSIVE_OEM_MANUFACTURERS
