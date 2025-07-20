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

    @Serializable
    internal object VideoCapture

    @Serializable
    internal object Stream

    @Serializable
    internal object Store

    @ConsistentCopyVisibility
    @Serializable
    internal data class WebView internal constructor(internal val url: String)

    @Serializable
    internal object Provider


    @Serializable
    internal object Service

    @Serializable
    internal object Notify
}