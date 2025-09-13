package edu.tyut.helloktorfit.ui.screen

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.contentValuesOf
import androidx.navigation.NavHostController
import edu.tyut.helloktorfit.manager.AudioExtractManager
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape10
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG: String = "MediaCodecScreen"

@Composable
internal fun MediaCodecScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
) {
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        coroutineScope.launch {
            snackBarHostState.showSnackbar("权限获取是否成功: ${map.values.all { it }}")
        }
    }
    val launcher:  ManagedActivityResultLauncher<Array<String>, Uri?>  = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.apply {
            Log.i(TAG, "MediaCodecScreen -> uri: $this")
            val audioExtractManager = AudioExtractManager()
            val contentValues: ContentValues = contentValuesOf(
                MediaStore.Audio.Media.DISPLAY_NAME to "output1.pcm",
                MediaStore.Audio.Media.MIME_TYPE to MediaFormat.MIMETYPE_AUDIO_RAW,
                MediaStore.Audio.Media.RELATIVE_PATH to Environment.DIRECTORY_MUSIC
            )
            // val mp3Uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //     // context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
            //     FileProvider.getUriForFile(context, "${context.packageName}.provider", File(
            //         Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            //         "output1.pcm"
            //     ))
            // } else {
            //     FileProvider.getUriForFile(context, "${context.packageName}.provider", File(
            //         Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
            //         "output1.pcm"
            //     ))
            // } ?: return@apply

            val yuvUri: Uri =
                FileProvider.getUriForFile(context, "${context.packageName}.provider", File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "output.yuv"
                ))

            val pcmUri: Uri =
                FileProvider.getUriForFile(context, "${context.packageName}.provider", File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "output.h264"
                ))

            // Log.i(TAG, "MediaCodecScreen -> mp3Uri: $mp3Uri")
            coroutineScope.launch(Dispatchers.IO){
                //  ffplay -x 1280 -y 720 -f rawvideo -pixel_format yuv420p -video_size 3840x2160 -framerate 60 output1.yuv
                Log.i(TAG, "MediaCodecScreen -> before ${Thread.currentThread()}")
                audioExtractManager.yuvToh264(context, this@apply, pcmUri)
                Log.i(TAG, "MediaCodecScreen -> after ${Thread.currentThread()}")
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "开发者",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                    context.startActivity(intent)
                },
            color = Color.White
        )
        Text(
            text = "发送信息",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    permissionLauncher.launch(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    launcher.launch(arrayOf("*/*"))
                },
            color = Color.White
        )
    }
}