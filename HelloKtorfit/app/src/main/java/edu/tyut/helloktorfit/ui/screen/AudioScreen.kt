package edu.tyut.helloktorfit.ui.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs
import kotlin.random.Random

private const val TAG: String = "AudioScreen"

private const val WINDOWS_SIZE: Int = 5
private const val BAR_SIZE: Int = 10
private const val BAR_HEIGHT: Int = 200

private var windowSize: Int = 0
private var percentSum: Float = 0F

@Composable
internal fun AudioScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
    helloViewModel: HelloViewModel = hiltViewModel<HelloViewModel>()
) {
    val context: Context = LocalContext.current
    val density: Density = LocalDensity.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val volumes = remember {
        mutableStateListOf<Float>(*FloatArray(BAR_SIZE).toTypedArray())
    }
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
                            "hello1.pcm"
                        ).apply {
                            Log.i(TAG, "AudioScreen path: $this")
                        }
                    )
                    val channel = Channel<Float>()
                    coroutineScope.launch {
                        for (percent in channel) {
                            // 方法2
                            percentSum += percent
                            windowSize++
                            if (windowSize >= WINDOWS_SIZE) {
                                for (i in 1..volumes.size / 2) {
                                    volumes[i - 1] = volumes[i]
                                    volumes[volumes.size - i] = volumes[volumes.size - 1 - i]
                                }
                                //  -tag:Battery -tag:oktorfit:binde -tag:ut.helloktorfi
                                volumes[volumes.size / 2] = percentSum / WINDOWS_SIZE
                                Log.i(TAG, "AudioScreen -> 平均值: ${percentSum / WINDOWS_SIZE}")
                                percentSum = 0F
                                windowSize = 0
                            }
                        }
                    }
                    coroutineScope.launch {
                        Log.i(TAG, "AudioScreen -> startRecord...")
                        recordManager.startRecord2(uri = uri) { percent: Float ->
                            // Log.i(TAG, "AudioScreen -> percent: $percent, Thread: ${Thread.currentThread()}")
                            channel.trySend(percent)
                        }
                        val int: Int = -2147483648
                        Log.i(TAG, "AudioScreen -> endRecord -> ${abs(int.toFloat())}")
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
        fun randomColor(): Color {
            val r = Random.nextInt(0, 256)
            val g = Random.nextInt(0, 256)
            val b = Random.nextInt(0, 256)
            return Color(r, g, b)
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(color = Color.Cyan)
        ) {
            for (i in 0 until BAR_SIZE) {
                drawRect(color = randomColor(), topLeft = Offset(10F + i * 25F, 0F), size = Size(20F, with(density){ ((volumes[i]).coerceIn(0F, 1F) * BAR_HEIGHT).dp.toPx() }))
            }
            for (i in 0 until BAR_SIZE) {
                drawRect(color = randomColor(), topLeft = Offset(260F + i * 25F, 0F), size = Size(20F, with(density){ ((volumes[i]).coerceIn(0F, 1F) * BAR_HEIGHT).dp.toPx() }))
            }
        }

        // Start-Process -FilePath ".\emulator.exe" -ArgumentList "-avd Pixel_6_Pro" -WindowStyle Hidden
        Text(
            text = "启动Activity",
            Modifier // /system/etc/init/hw/init.rc
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    val intent = Intent()
                    intent.setClassName(
                        "io.github.customview",
                        "io.github.customview.activity.MainActivity"
                    )
                    intent.putExtra("name", "ITGuoKe")
                    context.startActivity(intent)
                    Log.i(TAG, "AudioScreen -> activity: ${context.javaClass}")
                },
            color = Color.White
        )
    }
}