package edu.tyut.helloktorfit.route

import kotlinx.serialization.Serializable

internal sealed class Routes {
    @Serializable
    internal object Greeting
    @Serializable
    internal data class Hello(internal val name: String)
}