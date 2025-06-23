package edu.tyut.webviewlearn.ui.screen

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.DownloadListener
import android.webkit.JsPromptResult
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent
import edu.tyut.webviewlearn.BuildConfig
import edu.tyut.webviewlearn.hybrid.Hybrid
import edu.tyut.webviewlearn.spi.HybridServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val TAG: String = "WebViewScreen"
private const val HYBRID_SCHEME = "hybrid"

/**
 * 较为简单，会导致事件冲突
 */
@Composable
internal fun WebViewPanel1(
    snackBarHostState: SnackbarHostState,
    navHostController: NavHostController, url: String,
) {
    var logContent: String by remember {
        mutableStateOf(value = "")
    }
    val offsetX: Animatable<Float, AnimationVector1D> = remember {
        Animatable(initialValue = 0F)
    }
    val screenWidthPx: Float = with(receiver = LocalDensity.current) { // 用于滑出整个屏幕
        LocalContext.current.resources.displayMetrics.widthPixels.toFloat()
    }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(key1 = Unit) {
                if (!BuildConfig.DEBUG) return@pointerInput
                detectHorizontalDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetX.value <= -screenWidthPx / 2) {
                                offsetX.animateTo(targetValue = -screenWidthPx)
                            } else {
                                offsetX.animateTo(targetValue = 0F)
                            }
                        }
                    },
                    onHorizontalDrag = { pointerInputChange: PointerInputChange, offset: Float ->
                        pointerInputChange.consume()
                        val newValue: Float = (offsetX.value + offset).coerceIn(
                            minimumValue = -screenWidthPx,
                            maximumValue = 0F
                        )
                        coroutineScope.launch {
                            offsetX.snapTo(targetValue = newValue)
                        }
                    }
                )
            }
    ) {
        Text(
            text = logContent,
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .systemBarsPadding()
                .fillMaxSize(),
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 12.sp // 行高 = 字号，避免额外间距
            ),
        )
        WebViewScreen(
            modifier = Modifier
                .offset {
                    IntOffset(x = offsetX.value.roundToInt(), y = 0)
                }
                .fillMaxSize(),
            snackBarHostState = snackBarHostState,
            navHostController = navHostController,
            url = url
        ) { log: String ->
            logContent += log + "\n"
        }
    }
}

@Composable
internal fun WebViewPanel(
    snackBarHostState: SnackbarHostState,
    navHostController: NavHostController, url: String,
) {
    var logContent: String by remember {
        mutableStateOf(value = "")
    }
    val offsetX: Animatable<Float, AnimationVector1D> = remember {
        Animatable(initialValue = 0F)
    }
    val screenWidthPx: Float = with(receiver = LocalDensity.current) { // 用于滑出整个屏幕
        LocalContext.current.resources.displayMetrics.widthPixels.toFloat()
    }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(key1 = Unit) {
                if (!BuildConfig.DEBUG) return@pointerInput
                Log.i(TAG, "WebViewPanel -> pointerInput debug...")
                awaitPointerEventScope {
                    Log.i(TAG, "WebViewPanel -> awaitPointerEventScope...")
                    while (true) {
                        val event: PointerEvent = awaitPointerEvent()
                        val pressedPointers: List<PointerInputChange> =
                            event.changes.filter { it.pressed }
                        when (pressedPointers.size) {
                            2 -> {
                                pressedPointers.forEach { it.consume() }
                                val offset: Float =
                                    pressedPointers.map { it.position.x }.average().toFloat()
                                val newValue: Float = offset.coerceIn(
                                    minimumValue = 0F,
                                    maximumValue = screenWidthPx
                                )
                                coroutineScope.launch {
                                    offsetX.snapTo(targetValue = newValue - screenWidthPx)
                                }
                            }
                        }
                        if (pressedPointers.isEmpty()) {
                            coroutineScope.launch {
                                if (offsetX.value <= -screenWidthPx / 2) {
                                    offsetX.animateTo(targetValue = -screenWidthPx)
                                } else {
                                    offsetX.animateTo(targetValue = 0F)
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Box(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = logContent,
                modifier = Modifier
                    .verticalScroll(state = rememberScrollState())
                    .fillMaxSize(),
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 12.sp // 行高 = 字号，避免额外间距
                ),
            )
            Icon(
                imageVector = Icons.Default.Clear, contentDescription = "清除日志",
                tint = Color.Gray,
                modifier = Modifier
                    .clickable {
                        logContent = ""
                    }
                    .padding(all = 5.dp)
            )
        }
        WebViewScreen(
            modifier = Modifier
                .offset {
                    IntOffset(x = offsetX.value.roundToInt(), y = 0)
                }
                .fillMaxSize()
                .navigationBarsPadding(),
            snackBarHostState = snackBarHostState,
            navHostController = navHostController,
            url = url
        ) { log: String ->
            logContent += log + "\n"
        }
    }
}

@Composable
private fun WebViewScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState,
    navHostController: NavHostController,
    url: String,
    logContent: (log: String) -> Unit,
) {
    Log.i(TAG, "WebViewScreen...")
    val activity: Activity = LocalActivity.current!!
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val density: Density = LocalDensity.current
    val statusBarTopPadding: Int = with(receiver = density) {
        Log.i(TAG, "WebViewScreen -> with...")
        WindowInsets.statusBars.getTop(density = this).toDp().value.toInt()
    }

    val webView: WebView = remember {
        Log.i(TAG, "WebViewScreen remember...")
        val webViewEntryPoint: WebViewEntryPoint = EntryPointAccessors.fromActivity(
            activity = activity,
            entryPoint = WebViewEntryPoint::class.java
        )
        webViewEntryPoint.getWebView()
    }

    // back 事件处理
    BackHandler {
        Log.i(TAG, "WebViewScreen BackHandler...")
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            navHostController.popBackStack()
        }
    }

    Log.i(TAG, "WebViewScreen -> webView: $webView")
    Log.i(TAG, "WebViewScreen init -> url: $url")

    // 文件选择
    var fileCallback: ValueCallback<Array<out Uri?>?>? by remember {
        mutableStateOf(value = object : ValueCallback<Array<out Uri?>?> {
            override fun onReceiveValue(value: Array<out Uri?>?) {
            }
        })
    }
    val launcher: ManagedActivityResultLauncher<String, List<@JvmSuppressWildcards Uri>>
            = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris:  List<@JvmSuppressWildcards Uri> ->
        fileCallback?.onReceiveValue(uris.toTypedArray())
    }

    AndroidView(
        modifier = modifier,
        factory = { context: Context ->
            Log.i(TAG, "WebViewScreen -> factory...")
            webView.apply {
                Log.i(TAG, "WebViewScreen init ... ")
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                setDownloadListener(object : DownloadListener{
                    override fun onDownloadStart(
                        url: String?,
                        userAgent: String?,
                        contentDisposition: String?,
                        mimetype: String?,
                        contentLength: Long
                    ) {
                        Log.i(TAG, "onDownloadStart -> url: $url, userAgent: $userAgent, contentDisposition: $contentDisposition, mimetype: $mimetype, contentLength: $contentLength")

                        val uri: Uri? = url?.toUri()
                        val request: DownloadManager.Request = DownloadManager.Request(uri)
                        request.setTitle("文件下载")
                        request.setDescription("正在下载文件")

                        val fileName: String = URLUtil.guessFileName(url, contentDisposition, mimetype)
                        Log.i(TAG, "onDownloadStart -> url: $url, userAgent: $userAgent, contentDisposition: $contentDisposition, mimetype: $mimetype, contentLength: $contentLength, fileName: $fileName")
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        val downloadManager: DownloadManager? = context.getSystemService<DownloadManager>(DownloadManager::class.java)
                        downloadManager?.enqueue(request)

                        // Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar("开始下载${fileName}文件")
                        }
                    }
                })

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        val javaScript = """
                                window.addEventListener('load', () => {
                                    if(document.body.style){
                                        document.body.style.paddingTop = '${statusBarTopPadding}px'
                                    }
                                })
                        """.trimIndent()
                        webView.evaluateJavascript(javaScript) { value: String? ->
                            Log.i(TAG, "onPageStarted -> value: $value")
                        }
                    }
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.i(TAG, "onPageFinished...")
                    }
                }
                val hybridMap: Map<String, Hybrid> = HybridServiceManager.getHybrids()
                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: ValueCallback<Array<out Uri?>?>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        Log.i(TAG, "onShowFileChooser -> mode: ${fileChooserParams?.mode}, title: ${fileChooserParams?.title}, acceptTypes: ${fileChooserParams?.acceptTypes?.joinToString()}, filenameHint: ${fileChooserParams?.filenameHint}, isCaptureEnabled: ${fileChooserParams?.isCaptureEnabled}")
                        fileCallback = filePathCallback
                        launcher.launch(fileChooserParams?.acceptTypes?.getOrNull(index = 0) ?: "*/*")
                        return true
                    }
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        val log: String = consoleMessage?.message().toString()
                        Log.i(TAG, log)
                        logContent(log)
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
                        Log.i(
                            TAG,
                            "onJsPrompt -> url: $url, message: $message, defaultValue: $defaultValue, result: ${result.toString()}"
                        )
                        val uri: Uri? = message?.toUri()
                        if (HYBRID_SCHEME == uri?.scheme) {
                            coroutineScope.launch {
                                try {
                                    val resultValue: String? = hybridMap[uri.authority]?.onAction(
                                        context = context,
                                        webView = webView,
                                        navHostController = navHostController,
                                        snackBarHostState = snackBarHostState,
                                        uri = uri
                                    )
                                    result?.confirm(resultValue)
                                } catch (e: Exception) {
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
private interface WebViewEntryPoint {
    fun getWebView(): WebView
}