package edu.tyut.webviewlearn.hybrid.impl

import android.content.Context
import android.net.Uri
import android.webkit.WebView
import androidx.navigation.NavHostController
import edu.tyut.webviewlearn.hybrid.Hybrid

internal class RefreshHybrid internal constructor() : Hybrid {
    override fun getName(): String {
        return "refresh"
    }

    override fun onAction(
        context: Context,
        webView: WebView,
        navHostController: NavHostController,
        uri: Uri
    ): String {
        webView.reload()
        return "success"
    }
}