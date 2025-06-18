package edu.tyut.webviewlearn.ui.screen

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JsPromptResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import edu.tyut.webviewlearn.hybrid.Hybrid
import edu.tyut.webviewlearn.spi.HybridServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG: String = "WebViewScreen"
private const val HYBRID_SCHEME = "hybrid"

@Composable
internal fun WebViewScreen(
    snackBarHostState: SnackbarHostState,
    navHostController: NavHostController, url: String
){

    val activity: Activity = LocalActivity.current!!
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val density: Density = LocalDensity.current
    val statusBarTopPadding: Int = with(receiver = density){
        WindowInsets.statusBars.getTop(density = this).toDp().value.toInt()
    }

    val webView: WebView = remember {
        val webViewEntryPoint: WebViewEntryPoint = EntryPointAccessors.fromActivity(
            activity = activity,
            entryPoint = WebViewEntryPoint::class.java
        )
        webViewEntryPoint.getWebView()
    }

    // back 事件处理
    BackHandler {
        if(webView.canGoBack()) {
            webView.goBack()
        } else {
            navHostController.popBackStack()
        }
    }

    Log.i(TAG, "WebViewScreen -> webView: $webView")
    Log.i(TAG, "WebViewScreen init -> url: $url")
    AndroidView(
        modifier = Modifier
            .fillMaxSize(),
        factory = { context: Context ->
            webView.apply{
                Log.i(TAG, "WebViewScreen init ... ")
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        val javaScript = "document.body.style.paddingTop='${statusBarTopPadding}px'"
                        webView.evaluateJavascript(javaScript) { value: String? ->
                            Log.i(TAG, "onPageFinished -> value: $value")
                        }
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.i(TAG, consoleMessage?.message().toString())
                        return true
                    }
                    override fun onJsPrompt(
                        view: WebView?,
                        url: String?,
                        message: String?,
                        defaultValue: String?,
                        result: JsPromptResult?
                    ): Boolean {
                        // TODO something
                        Log.i(TAG, "onJsPrompt -> url: $url, message: $message, defaultValue: $defaultValue, result: ${result.toString()}")
                        val hybridMap: Map<String, Hybrid> = HybridServiceManager.getHybrids()
                        val uri: Uri? = message?.toUri()
                        if(HYBRID_SCHEME == uri?.scheme) {
                            coroutineScope.launch {
                                try {
                                    val resultValue: String? = hybridMap[uri.authority]?.onAction(context = context, webView = webView, navHostController = navHostController, snackBarHostState = snackBarHostState, uri = uri)
                                    result?.confirm(resultValue)
                                } catch (e: Exception){
                                    result?.confirm(e.stackTraceToString())
                                }
                            }
                            return true
                        }
                        return super.onJsPrompt(view, url, message, defaultValue, result)
                    }
                }
            }
        },
        update = {
            Log.i(TAG, "WebViewScreen update -> url: $url")
            it.loadUrl(url)
        }
    )
}

@EntryPoint
@InstallIn(value = [ActivityComponent::class])
private interface WebViewEntryPoint{
    fun getWebView(): WebView
}