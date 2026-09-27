package com.lifevault.app.core.files

/** The Coil "model" type for a vault attachment — request `VaultImageRef(id)`, not a `Uri`. */
data class VaultImageRef(val attachmentId: String, val thumb: Boolean = false)
