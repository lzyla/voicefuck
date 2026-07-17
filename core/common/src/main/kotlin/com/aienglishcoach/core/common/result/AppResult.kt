package com.aienglishcoach.core.common.result

/**
 * Lightweight functional result used across use cases and repositories.
 *
 * Unlike [kotlin.Result] it carries a typed [AppError] instead of a raw
 * [Throwable], which keeps error handling exhaustive and UI-friendly.
 */
sealed interface AppResult<out T> {

    data class Success<T>(val value: T) : AppResult<T>

    data class Failure(val error: AppError) : AppResult<Nothing>

    companion object {
        fun <T> success(value: T): AppResult<T> = Success(value)
        fun failure(error: AppError): AppResult<Nothing> = Failure(error)
    }
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) action(value)
    return this
}

inline fun <T> AppResult<T>.onFailure(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Failure) action(error)
    return this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.value

fun <T> AppResult<T>.errorOrNull(): AppError? = (this as? AppResult.Failure)?.error

val AppResult<*>.isSuccess: Boolean get() = this is AppResult.Success
