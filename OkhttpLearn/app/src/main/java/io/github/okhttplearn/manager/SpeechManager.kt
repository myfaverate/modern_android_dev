package io.github.okhttplearn.manager

import android.content.Context
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale

private const val TAG: String = "SpeechManager"

internal class SpeechManager internal constructor(
    private val context: Context,
){

    private val textToSpeech: TextToSpeech =
        TextToSpeech(context) { status: Int ->
            if (status == TextToSpeech.SUCCESS){
                Toast.makeText(context, "tts初始化成功", Toast.LENGTH_SHORT).show()
                initTts()
            } else {
                Toast.makeText(context, "tts初始化失败", Toast.LENGTH_SHORT).show()
            }
        }

    private fun initTts(){
        Log.i(TAG, "init...")
        textToSpeech.apply {
            val status: Int = setLanguage(Locale.CHINA)
            if (status == TextToSpeech.LANG_MISSING_DATA || status == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "tts不支持中文", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "tts支持中文", Toast.LENGTH_SHORT).show()
            }
        }
    }
    internal fun speech(message: String){
        textToSpeech.speak(message, TextToSpeech.QUEUE_ADD, null, "tts1")
        textToSpeech.voices?.map { it.locale.language }?.distinct()?.apply {
            Log.i(TAG, "speech -> apple")
        }
    }
    internal fun destroy(){
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}