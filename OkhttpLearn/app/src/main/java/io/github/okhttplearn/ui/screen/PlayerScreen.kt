package io.github.okhttplearn.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import io.github.okhttplearn.R
import io.github.okhttplearn.ui.theme.OkhttpLearnTheme
import tv.danmaku.ijk.media.player.IjkMediaPlayer

private const val TAG: String = "PlayerScreen"

@Composable
internal fun PlayerScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
) {
    val context: Context = LocalContext.current
    val ijkMediaPlayer: IjkMediaPlayer = remember {
        IjkMediaPlayer()
    }
    Box(modifier = modifier.fillMaxSize()){
        AndroidView(
            factory = { viewContext: Context ->
                SurfaceView(viewContext).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    Log.i(TAG, "PlayerScreen setSurface1 -> ")
                    // ijkMediaPlayer.setSurface(this.holder.surface)
                    this.holder.addCallback(object : SurfaceHolder.Callback2{
                        override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
                            Log.i(TAG, "surfaceRedrawNeeded...")
                        }

                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {
                            Log.i(TAG, "surfaceChanged -> format: $format, width: $width, height: $height")
                        }

                        override fun surfaceCreated(holder: SurfaceHolder) {
                            ijkMediaPlayer.setSurface(holder.surface)
                            Log.i(TAG, "surfaceCreated 3...")
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            Log.i(TAG, "surfaceDestroyed...")
                        }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Text(text = "播放", modifier = Modifier.clickable {
            // val videoUri: Uri = Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            //     .path(R.raw.question1.toString()).build()
            // ijkMediaPlayer.setDataSource(context, videoUri)
            ijkMediaPlayer.dataSource = "/data/data/io.github.okhttplearn/files/question1.mp4"
            ijkMediaPlayer.prepareAsync()
        })
    }
    DisposableEffect(key1 = Unit) {
        Log.i(TAG, "PlayerScreen -> setSurface2...")
        onDispose {
            ijkMediaPlayer.release()
            IjkMediaPlayer.native_profileEnd()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorldScreenPreview() {
    OkhttpLearnTheme {
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        PlayerScreen(modifier = Modifier, snackBarHostState = snackBarHostState)
    }
}