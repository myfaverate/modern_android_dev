package edu.tyut.webviewlearn.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import edu.tyut.webviewlearn.video.CaptureManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG: String = "VideoScreen"

@Composable
internal fun VideoScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState
){
    val context: Context = LocalContext.current
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val captureManager by remember {
        mutableStateOf(value = CaptureManager(context))
    }
    val permissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        arrayOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { map: Map<String, Boolean> ->
            val isSuccess: Boolean = map.any { it.value }
            coroutineScope.launch {
                snackBarHostState.showSnackbar("获取权限${if (isSuccess) "成功" else "失败"}")
            }
        }
    Box(
        modifier = modifier,
    ){
        AndroidView(
            factory = { viewContext: Context ->
                SurfaceView(viewContext).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    captureManager.preview.setSurfaceProvider { request: SurfaceRequest ->
                        val surface = holder.surface
                        request.provideSurface(
                            surface,
                            ContextCompat.getMainExecutor(context)
                        ) { result: SurfaceRequest.Result ->
                            // 处理释放等
                            Log.i(TAG, "setSurfaceProvider -> result: $result")
                        }
                    }
                }
            },
            update = { surfaceView: SurfaceView ->
                Log.i(TAG, "VideoScreen...")
            }
        )
        Text(text = "开始录像",
            color = Color.White, modifier = Modifier
                .align(Alignment.BottomStart)
                .clickable {
            if (permissions.any { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }){
                launcher.launch(permissions)
                return@clickable
            }
            captureManager.start(lifecycleOwner = lifecycleOwner, videoName = "hello.mp4")
        })
        Text(text = "停止录像", color = Color.White, modifier = Modifier
            .align(Alignment.TopStart)
            .clickable{
            captureManager.stop()
        })
    }
}