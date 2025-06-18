package edu.tyut.webviewlearn.hybrid

import android.content.Context
import android.net.Uri
import android.webkit.WebView
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController

internal interface Hybrid {
    fun getName(): String
    suspend fun onAction(
        context: Context, webView: WebView, navHostController: NavHostController, uri: Uri,
        snackBarHostState: SnackbarHostState,
    ): String
}