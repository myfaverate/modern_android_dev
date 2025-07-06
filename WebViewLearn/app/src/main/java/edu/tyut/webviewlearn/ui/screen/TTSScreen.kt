package edu.tyut.webviewlearn.ui.screen

import android.content.Context
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

private const val TAG: String = "TTSScreen"

@Composable
internal fun TTSScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState
) {
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val tts: TextToSpeech by remember {
        val textToSpeech = TextToSpeech(context) { status: Int ->
            if (status == TextToSpeech.SUCCESS) {
                Log.i(TAG, "TTSScreen -> tts init success")
            } else {
                Log.i(TAG, "TTSScreen -> tts init failure")
            }
        }
        val result: Int = textToSpeech.setLanguage(java.util.Locale.ENGLISH)
        if (TextToSpeech.LANG_MISSING_DATA == result || TextToSpeech.LANG_NOT_SUPPORTED == result) {
            Log.i(TAG, "TTSScreen -> 语言不支持")
        } else {
            Log.i(TAG, "TTSScreen -> 语言支持")
        }
        mutableStateOf(value = textToSpeech)
    }
    var text: String by remember {
        mutableStateOf(value = "")
    }
    DisposableEffect(key1 = Unit) {
        onDispose {
            tts.shutdown()
        }
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TextField(value = text, placeholder = {
            Text(text = "请输入TTS")
        }, onValueChange = { text = it })
        Text(text = "朗读", modifier = Modifier.clickable {
            if (text.isEmpty()) {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar("请输入tts文本")
                }
                return@clickable
            }
            val result: Int = tts.speak(text, TextToSpeech.QUEUE_FLUSH, bundleOf(), "utteranceId")
            coroutineScope.launch {
                snackBarHostState.showSnackbar("朗读${if(result == TextToSpeech.SUCCESS) "成功" else "失败"}")
            }
        })
        Text(text = "生成pcm", modifier = Modifier.clickable {
            if (text.isEmpty()) {
                coroutineScope.launch {
                    snackBarHostState.showSnackbar("请输入tts文本")
                }
                return@clickable
            }
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onDone(utteranceId: String?) {
                    Log.i(TAG, "onDone -> utteranceId: $utteranceId")
                }

                override fun onError(utteranceId: String?) {
                    Log.i(TAG, "onError -> utteranceId: $utteranceId")
                }

                override fun onStart(utteranceId: String?) {
                    Log.i(TAG, "onStart -> utteranceId: $utteranceId")
                }
            })
            val file =
                File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/hello1.wav")
            val result = tts.synthesizeToFile(
                text,
                bundleOf(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID to "ttsOutput"),
                file,
                "ttsOutput"
            )
            if (result == TextToSpeech.SUCCESS) {
                Log.d(TAG, "合成任务已提交: ${file.absolutePath}")
            }
        })
    }
}