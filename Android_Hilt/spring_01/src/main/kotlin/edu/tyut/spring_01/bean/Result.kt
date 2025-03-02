package edu.tyut.spring_01.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Result {

    // Success 子类
    @Serializable
    data class Success<T>(
        val code: Int,
        @SerialName(value = "message")
        val message: String,
        @SerialName(value = "data")
        val data: T
    ) : Result()

    // Failure 子类
    @Serializable
    data class Failure<T>(
        val code: Int,
        @SerialName(value = "message")
        val message: String,
        @SerialName(value = "data")
        val data: T
    ) : Result()

    companion object {
        private const val SUCCESS: Int = 200
        private const val FAILURE: Int = 500
        fun <T> success(message: String, data: T): Success<T> {
            return Success(code = SUCCESS, message = message, data = data)
        }
        fun <T> failure(message: String, data: T): Failure<T> {
            return Failure(code = FAILURE, message = message, data = data)
        }
    }
}
