package edu.tyut.helloktorfit.data.bean

import kotlinx.serialization.Serializable

@Serializable
internal data class Result <T> (
    internal val code: Int,
    internal val message: String,
    internal val data: T,
) {
    internal companion object {
        private const val SUCCESS: Int = 200
        private const val FAILURE: Int = 500
        internal fun <T> success(message: String, data: T) = Result<T>(code = SUCCESS, message = message, data = data)
        internal fun <T> failure(message: String, data: T) = Result<T>(code = FAILURE, message = message, data = data)
    }
}

// @Serializable
// internal sealed class Result1 {
//
//     @Serializable
//     internal data class Success<T>(
//         internal val code: Int,
//         internal val message: String,
//         internal val data: T
//     ) : Result1()
//
//     @Serializable
//     internal data class Failure<T>(
//         internal val code: Int,
//         internal val message: String,
//         internal val data: T
//     ) : Result1()
//
//     internal companion object {
//         private const val SUCCESS: Int = 200
//         private const val FAILURE: Int = 500
//         internal fun <T> success(message: String, data: T): Success<T> {
//             return Success(code = SUCCESS, message = message, data = data)
//         }
//
//         internal fun <T> failure(message: String, data: T): Failure<T> {
//             return Failure(code = FAILURE, message = message, data = data)
//         }
//     }
// }
