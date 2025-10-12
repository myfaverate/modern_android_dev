package io.github.opensllearn.ui.screen

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.IntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.opensllearn.ui.theme.OpenSLLearnTheme
import kotlin.math.min

private const val TAG: String = "VideoScreen"

@OptIn(markerClass = [UnstableApi::class])
@Composable
internal fun VideoScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState
) {
    val context: Context = LocalContext.current
    val density: Density = LocalDensity.current
    val configuration: Configuration = LocalConfiguration.current
    val exoPlayer: ExoPlayer by remember {
        val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
            .build()
        exoPlayer.addMediaItem(MediaItem.fromUri("http://192.168.31.90:8080/video1.mp4"))
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
        mutableStateOf(value = exoPlayer)
    }
    var viewSize: IntSize by remember {
        mutableStateOf(value = IntSize.Zero)
    }
    var videoWidth: Int by remember {
        mutableIntStateOf(value = configuration.screenWidthDp)
    }
    var videoHeight: Int by remember {
        mutableIntStateOf(value = configuration.screenHeightDp)
    }
    DisposableEffect(key1 = Unit) {
        val eventListener: Player.Listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "onPlayerError play error: ${error.message}", error)
            }
            override fun onPlayerErrorChanged(error: PlaybackException?) {
                Log.e(TAG, "onPlayerErrorChanged play error: ${error?.message}", error)
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                val scale: Float = min(viewSize.width / videoSize.width.toFloat(), viewSize.height / videoSize.height.toFloat())
                videoWidth = (videoSize.width * scale).toInt()
                videoHeight = (videoSize.height * scale).toInt()
                Log.i(TAG, "onVideoSizeChanged -> videoWidth: ${videoSize.width}, videoHeight: ${videoSize.height}, width: $videoWidth, height: $videoHeight")
            }
        }
        Log.i(TAG, "MusicScreen -> 注册监听器")
        exoPlayer.addListener(eventListener)
        onDispose {
            exoPlayer.removeListener(eventListener)
            exoPlayer.stop()
            exoPlayer.release()
            Log.i(TAG, "MusicScreen -> 移除监听器")
        }
    }
    AndroidView(
        modifier = Modifier
            .width(width = videoWidth.dp)
            .height(height = videoHeight.dp)
            .onSizeChanged { intSize: IntSize ->
                viewSize = intSize
                Log.i(TAG, "VideoScreen -> intSize: $intSize")
            },
        factory = { viewContext: Context ->
            SurfaceView(viewContext).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                exoPlayer.setVideoSurface(holder.surface)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun VideoScreenPreview() {
    OpenSLLearnTheme {
        val navHostController: NavHostController = rememberNavController()
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        VideoScreen(navHostController = navHostController, snackBarHostState = snackBarHostState)
    }
}