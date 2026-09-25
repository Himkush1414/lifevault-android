package com.lifevault.app.core.domain.lock

/** Section 8.3: 6-digit PIN, rejecting the weakest choices. */
const val PIN_LENGTH = 6

/**
 * Curated common/weak 6-digit PINs (Section 8.3 "top-50 common PINs"). This is a
 * best-effort list, not derived from a breach-frequency dataset — see BUILD_LOG.md
 * Step 8 for the caveat and a pointer to tighten it with real data if desired.
 */
val COMMON_WEAK_PINS: Set<String> = setOf(
    "123123", "321321", "112233", "121212", "123321", "102030", "010101", "202020",
    "090909", "111222", "222111", "101010", "110011", "112211", "123000", "000123",
    "101101", "100200", "300400", "200300", "123654", "456123", "789456", "147258",
    "258147", "369258", "159753", "753159", "147147", "258258", "369369", "159159",
    "112358", "135791", "246810", "192837", "918273", "864209", "013579", "024680",
    "111333", "333111", "222444", "444222", "555000", "000555", "121121", "212212",
    "313313", "414414",
)

sealed interface PinValidationResult {
    data object Valid : PinValidationResult
    data object WrongLength : PinValidationResult
    data object NotAllDigits : PinValidationResult
    data object AllSameDigit : PinValidationResult
    data object SequentialDigits : PinValidationResult
    data object CommonlyUsed : PinValidationResult
}

/** Section 8.3: rejects all-same-digit, straight ascending/descending, and common PINs. */
fun validatePin(pin: String): PinValidationResult {
    if (pin.length != PIN_LENGTH) return PinValidationResult.WrongLength
    if (pin.any { !it.isDigit() }) return PinValidationResult.NotAllDigits

    val digits = pin.map { it - '0' }
    if (digits.toSet().size == 1) return PinValidationResult.AllSameDigit

    val ascending = digits.zipWithNext().all { (a, b) -> b - a == 1 }
    val descending = digits.zipWithNext().all { (a, b) -> a - b == 1 }
    if (ascending || descending) return PinValidationResult.SequentialDigits

    if (pin in COMMON_WEAK_PINS) return PinValidationResult.CommonlyUsed

    return PinValidationResult.Valid
}
