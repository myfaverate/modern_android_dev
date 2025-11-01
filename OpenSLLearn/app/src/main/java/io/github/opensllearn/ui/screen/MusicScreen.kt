package io.github.opensllearn.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.Trace
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.opensllearn.R
import io.github.opensllearn.ui.theme.OpenSLLearnTheme
import io.github.opensllearn.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

private const val TAG: String = "MusicScreen"

/**
 * ffmpeg -i music.ogg -f s16le -ar 44100 -ac 2 output.pcm
 * ffplay -f s16le -ch_layout stereo  -ar 44100 -i  .\output.pcm
 * https://www.cnblogs.com/blovecat/articles/18281859
 * https://gitee.com/frank2020/ExoPlayer
 * https://cmder.github.io/%E9%9F%B3%E8%A7%86%E9%A2%91/ExoPlayer/%E5%A6%82%E4%BD%95%E4%BB%8E%E6%92%AD%E6%94%BE%E5%99%A8%E6%9E%84%E9%80%A0%E8%A7%92%E5%BA%A6%E7%A0%94%E7%A9%B6-ExoPlayer-%E6%BA%90%E7%A0%81.html
 */

@Composable
internal fun MusicScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState
) {
    val context: Context = LocalContext.current
    var ptr: Long by remember {
        mutableLongStateOf(value = 0)
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ){
        Text(text = "创建录音22对象", modifier = Modifier
            .background(color = Color.Red)
            .padding(all = 5.dp)
            .clickable { // ffplay -f rawvideo -pixel_format yuv420p -video_size 640x480 yuv.yuv
                val yuvFile = File(context.cacheDir, "yuv.yuv")
                ptr = Utils.initCamera(640, 480, yuvFile.absolutePath)
            })
        Text(text = "销毁录音对象", modifier = Modifier.clickable {
            Utils.releaseCamera(ptr)
        })
    }
}

@Preview(showBackground = true)
@Composable
private fun MusicScreenPreview() {
    OpenSLLearnTheme {
        val navHostController: NavHostController = rememberNavController()
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        MusicScreen(navHostController = navHostController, snackBarHostState = snackBarHostState)
    }
}
/*
https://keeplooking.top/2023/08/12/Android/compose_inspector/
 */