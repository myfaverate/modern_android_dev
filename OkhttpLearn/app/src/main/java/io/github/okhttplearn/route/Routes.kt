package io.github.okhttplearn.route

import androidx.navigation3.runtime.NavKey
import io.github.okhttplearn.data.bean.Person
import kotlinx.serialization.Serializable

internal sealed class Routes : NavKey {
    @Serializable
    internal data class Greeting(internal val message: String) : Routes()
    @Serializable
    internal data class Detail(internal val person: Person) : Routes()
    @Serializable
    internal object Home : Routes()
    @Serializable
    internal object World : Routes()
    @Serializable
    internal object Player : Routes()
}