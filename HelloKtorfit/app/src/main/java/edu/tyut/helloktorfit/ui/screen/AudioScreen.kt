package edu.tyut.helloktorfit.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import edu.tyut.helloktorfit.manager.AudioRecordManager
import edu.tyut.helloktorfit.manager.AudioTrackManager
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape10
import edu.tyut.helloktorfit.viewmodel.HelloViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val TAG: String = "GifScreen"
// TODO
@Composable
internal fun AudioScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
    helloViewModel: HelloViewModel = hiltViewModel<HelloViewModel>()
) {
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val recordManager: AudioRecordManager by remember {
        mutableStateOf(value = AudioRecordManager(context = context))
    }
    val audioTrackManager: AudioTrackManager by remember {
        mutableStateOf(value = AudioTrackManager())
    }
    val permissions: Array<String> = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        coroutineScope.launch {
            snackBarHostState.showSnackbar("获取权限是否成功: ${map.values.all { it }}")
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "开始录音",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    if (permissions.any {
                            ContextCompat.checkSelfPermission(
                                context,
                                it
                            ) != PackageManager.PERMISSION_GRANTED
                        }) {
                        launcher.launch(permissions)
                        return@clickable
                    }
                    val uri: Uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.provider", File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "hello.pcm"
                        ).apply {
                            Log.i(TAG, "AudioScreen path: $this")
                        }
                    )
                    coroutineScope.launch {
                        Log.i(TAG, "AudioScreen -> startRecord...")
                        recordManager.startRecord(uri = uri)
                        Log.i(TAG, "AudioScreen -> endRecord...")
                    }
                },
            color = Color.White
        )
        Text(
            text = "停止录音",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    recordManager.stopRecord()
                },
            color = Color.White
        )
        Text(
            text = "播放录音",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    val uri: Uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.provider", File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                            "hello.pcm"
                        )
                    )
                    coroutineScope.launch {
                        audioTrackManager.startPlay(context, uri)
                    }
                },
            color = Color.White
        )
        Text(
            text = "暂停播放录音",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    audioTrackManager.pause()
                },
            color = Color.White
        )
    }
}