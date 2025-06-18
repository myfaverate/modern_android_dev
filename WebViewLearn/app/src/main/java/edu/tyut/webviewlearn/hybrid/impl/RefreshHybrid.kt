package edu.tyut.webviewlearn.hybrid.impl

import android.content.Context
import android.net.Uri
import android.webkit.WebView
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController
import edu.tyut.webviewlearn.hybrid.Hybrid
import kotlinx.coroutines.CoroutineScope

internal class RefreshHybrid internal constructor() : Hybrid {
    override fun getName(): String {
        return "refresh"
    }

    override suspend fun onAction(
        context: Context, webView: WebView, navHostController: NavHostController, uri: Uri,
        snackBarHostState: SnackbarHostState,
    ): String {
        webView.reload()
        return "success"
    }
}