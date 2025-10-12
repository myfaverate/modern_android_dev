package io.github.opensllearn.route

import kotlinx.serialization.Serializable

internal sealed class Routes {
    @Serializable
    internal object Greeting
    @Serializable
    internal object Music
    @Serializable
    internal object Video
}