package edu.tyut.webviewlearn.route

import kotlinx.serialization.Serializable

internal sealed class Routes {
    @Serializable
    internal object Greeting

    @Serializable
    internal object Hello

    @ConsistentCopyVisibility
    @Serializable
    internal data class WebView internal constructor(internal val url: String)
}