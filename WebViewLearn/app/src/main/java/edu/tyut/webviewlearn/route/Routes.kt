package edu.tyut.webviewlearn.route

import kotlinx.serialization.Serializable

internal sealed class Routes {
    @Serializable
    internal object Greeting

    @Serializable
    internal object Hello


    @Serializable
    internal object Voice


    @Serializable
    internal object TTS

    @ConsistentCopyVisibility
    @Serializable
    internal data class WebView internal constructor(internal val url: String)
}