package edu.tyut.webviewlearn.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import edu.tyut.webviewlearn.ui.theme.RoundedCornerShape10
import edu.tyut.webviewlearn.voice.VoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

private const val TAG: String = "VoiceScreen"

@Composable
internal fun VoiceScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState
) {
    val context: Context = LocalContext.current
    val audioManager: AudioManager =
        context.getSystemService<AudioManager>(AudioManager::class.java)
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val voiceManager: VoiceManager by remember {
        mutableStateOf(value = VoiceManager(context))
    }
    var isStart: Boolean by remember {
        mutableStateOf(value = voiceManager.isRecording)
    }
    val permissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
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
            val isSuccess: Boolean = map.all { it.value }
            coroutineScope.launch {
                snackBarHostState.showSnackbar("获取权限${if (isSuccess) "成功" else "失败"}")
            }
        }
    val uri: Uri by lazy {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/hello.pcm").apply {
                Log.i(TAG, "VoiceScreen -> path: $this")
            })
    }
    DisposableEffect(key1 = Unit) {
        val audioDeviceCallback: AudioDeviceCallback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo?>?) {
                Log.i(TAG, "onAudioDevicesAdded -> addedDevices: ${addedDevices?.joinToString()}")
                // AudioDeviceInfo.TYPE_BLE_HEADSET -> 26
            }

            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo?>?) {
                Log.i(
                    TAG,
                    "onAudioDevicesRemoved -> removedDevices: ${removedDevices?.joinToString()}"
                )
            }
        }
        audioManager.registerAudioDeviceCallback(
            audioDeviceCallback,
            Handler(Looper.getMainLooper())
        )
        onDispose {
            voiceManager.release()
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        }
    }
    Column(
        modifier = modifier
    ) {
        Text(
            text = if (isStart) "停止录音" else "开始录音", modifier = Modifier
                .background(color = Color.Cyan, shape = RoundedCornerShape10)
                .padding(all = 10.dp)
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
                    if (isStart) {
                        voiceManager.stopRecord()
                    } else {
                        coroutineScope.launch {
                            voiceManager.startRecord(context, uri)
                        }
                    }
                    isStart = !isStart
                    Log.i(TAG, "VoiceScreen -> isRecording: ${voiceManager.isRecording}")
                })
    }
}