package edu.tyut.webviewlearn.di

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import edu.tyut.webviewlearn.BuildConfig

@Module
@InstallIn(value = [ActivityComponent::class])
internal class WebViewModule internal constructor(){
    @ActivityScoped
    @Provides
    internal fun providerWebView(@ActivityContext context: Context): WebView {
        val webView = WebView(context) // webView是有兼容性的, 没有webview内核会崩溃，例如 adb shell pm uninstall --user 0 com.android.webview
        // webSettings
        val webSettings: WebSettings = webView.settings
        // 混合模式
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        // 运行 js 代码
        @SuppressLint("SetJavaScriptEnabled")
        webSettings.javaScriptEnabled = true
        // 允许 SessionStorage / LocalStorage 存储
        webSettings.domStorageEnabled = true
        // 禁用缩放
        webSettings.displayZoomControls = false
        webSettings.builtInZoomControls = false
        // 禁用文字缩放
        webSettings.textZoom = 100
        // 允许 WebView 使用 File 协议
        webSettings.allowFileAccess = true
        // 自动加载图片
        webSettings.loadsImagesAutomatically = true

        // 调试
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // webView 防止漏洞
        webView.removeJavascriptInterface("searchBoxJavaBridge_")
        webView.removeJavascriptInterface("accessibility")
        webView.removeJavascriptInterface("accessibilityTraversal")
        return webView
    }
}