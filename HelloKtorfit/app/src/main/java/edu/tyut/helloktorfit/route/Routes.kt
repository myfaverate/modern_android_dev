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
    @Serializable
    internal object Service
    @Serializable
    internal object Crop
    @Serializable
    internal object Image
    @Serializable
    internal object Gif
    @Serializable
    internal object Audio
    @Serializable
    internal object Binder
    @Serializable
    internal object System
    @Serializable
    internal object MediaCodec
}