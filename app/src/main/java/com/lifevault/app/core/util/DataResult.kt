package com.lifevault.app.core.util

/**
 * A success/failure wrapper for repository and use-case results (Section 2.1). Named
 * `DataResult` rather than `Result` to avoid clashing with `kotlin.Result`.
 */
sealed interface DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>
    data class Failure(val error: Throwable) : DataResult<Nothing>
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

inline fun <T> DataResult<T>.onSuccess(action: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) action(data)
    return this
}

inline fun <T> DataResult<T>.onFailure(action: (Throwable) -> Unit): DataResult<T> {
    if (this is DataResult.Failure) action(error)
    return this
}
