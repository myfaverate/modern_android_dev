package edu.tyut.webviewlearn.hybrid.impl

import android.content.Context
import android.net.Uri
import android.webkit.WebView
import android.widget.Toast
import androidx.navigation.NavHostController
import edu.tyut.webviewlearn.hybrid.Hybrid

/**
 * const HYBRID: String = "hybrid"
 * let result: String | null = prompt(`${Constants.HYBRID}://startShopPage?page=shop&shop=${JSON.stringify(shop)}`)
 * document.writeln(`startShopPage -> result: ${result}<br/>`)
 */
internal class HelloHybrid internal constructor(): Hybrid {
    override fun getName(): String {
        return "helloWorld"
    }

    override fun onAction(
        context: Context,
        webView: WebView,
        navHostController: NavHostController,
        uri: Uri
    ): String {
        Toast.makeText(context, uri.toString(), Toast.LENGTH_SHORT).show()
        return "hello"
    }

}