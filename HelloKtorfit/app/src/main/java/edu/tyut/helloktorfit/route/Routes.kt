package edu.tyut.helloktorfit.route

import edu.tyut.helloktorfit.data.bean.Photo
import kotlinx.serialization.Serializable

internal sealed class Routes {
    @Serializable
    internal object Greeting
    @Serializable
    internal data class Hello(internal val name: String)
    @Serializable
    internal data class PhotoScreen(internal val photo: Photo)
    @Serializable
    internal object Provider
}