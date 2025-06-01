package edu.tyut.helloktorfit.utils

import kotlinx.serialization.json.Json

internal object Constants {
    internal const val BASE_URL: String = "http://192.168.31.90:8080/"
    internal val JSON = Json {
        ignoreUnknownKeys = true
    }
}