package edu.tyut.helloktorfit.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.buffer
import okio.sink

private const val TAG: String = "AudioRecordManager"

internal class AudioRecordManager internal constructor(
    private val context: Context
) {
    private val channelMask: Int = AudioFormat.CHANNEL_IN_MONO
    private val sampleRate = 16000
    private val bufferSize: Int =
        AudioRecord.getMinBufferSize(sampleRate, channelMask, AudioFormat.ENCODING_PCM_16BIT)

    private val audioRecord: AudioRecord by lazy {
        initAudioRecord()
    }

    private fun initAudioRecord(): AudioRecord {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw RuntimeException("Not RECORD_AUDIO permission...")
        }
        return AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate).setChannelMask(channelMask).build()
            )
            .setBufferSizeInBytes(bufferSize)
            .build()
    }
    internal suspend fun startRecord(uri: Uri): Unit = withContext(Dispatchers.IO){
        audioRecord.startRecording()
        context.contentResolver.openOutputStream(uri)?.sink()?.buffer()
            ?.use { bufferedSink: BufferedSink ->
                var totalLength = 0L
                val bytes = ByteArray(bufferSize)
                var length: Int
                while (audioRecord.read(bytes, 0, bytes.size).also { length = it } > 0) {
                    Log.i(TAG, "startRecord -> data: ${bytes.joinToString()}")
                    bufferedSink.write(bytes, 0, length)
                    totalLength += length
                }
                bufferedSink.flush()
                Log.i(TAG, "startRecord -> 录制完成, 文件大小为: $totalLength bytes")
            }
    }
    // @RequiresPermission(value = Manifest.permission.RECORD_AUDIO)
    internal fun stopRecord(){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw RuntimeException("Not RECORD_AUDIO permission...")
        }
        if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop()
        }
    }
    internal fun release(){
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw RuntimeException("Not RECORD_AUDIO permission...")
        }
        audioRecord.release()
    }
}