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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

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

    /**
     * 0000 -> 0
     * 0001 -> 1
     * 0010 -> 2
     * 0011 -> 3
     * 0100 -> 4
     * 0101 -> 5
     * 0110 -> 6
     * 0111 -> 7
     * 1000 -> 8
     * 1001 -> 9
     * 1010 -> A
     * 1011 -> B
     * 1100 -> C
     * 1101 -> D
     * 1110 -> E
     * 1111 -> F
     *
     * byte  0000 0000
     * short 0000 0000 0000 0000
     * callback: (percent: Float) -> Unit sub thread
     */
    internal suspend fun startRecord(uri: Uri, callback: (percent: Float) -> Unit): Unit = withContext(Dispatchers.IO){ // 40 ms 25 个 是一秒
        audioRecord.startRecording()
        context.contentResolver.openOutputStream(uri)?.sink()?.buffer()
            ?.use { bufferedSink: BufferedSink ->
                var totalLength = 0L
                val bytes = ByteArray(bufferSize) // 1280 -> 640
                var length: Int
                while (audioRecord.read(bytes, 0, bytes.size).also { length = it } > 0) {
                    bufferedSink.write(bytes, 0, length)
                    totalLength += length
                    // 求最大的 percent
                    var minShort: Short = Short.MAX_VALUE
                    var maxShort: Short = Short.MIN_VALUE
                    for (i in 0 until length - 1 step 2){
                        val low: Int = bytes[i].toInt() and 0xFF // 小端
                        val high: Int = bytes[i + 1].toInt() shl 8
                        val shortValue: Short = (low or high).toShort()
                        maxShort = maxOf(maxShort, shortValue)
                        minShort = minOf(minShort, shortValue)
                    }
                    val percent: Float = max(maxShort.toFloat(), abs(minShort.toFloat())) / Short.MAX_VALUE.toFloat() // 归一
                    callback(percent)
                    Log.i(TAG, "startRecord -> size: ${bytes.size}, percent: ${percent}, maxShort: $maxShort, minShort: $minShort, data: ${bytes.joinToString{ it.toHexString() }}")
                }
                bufferedSink.flush()
                Log.i(TAG, "startRecord -> 录制完成, 文件大小为: $totalLength bytes")
            }
    }

    internal suspend fun startRecord2(uri: Uri, callback: (percent: Float) -> Unit): Unit = withContext(Dispatchers.IO){ // 40 ms 25 个 是一秒
        audioRecord.startRecording()
        context.contentResolver.openOutputStream(uri)?.sink()?.buffer()
            ?.use { bufferedSink: BufferedSink ->
                var totalLength = 0L
                val bytes = ByteArray(bufferSize) // 1280 -> 640
                var length: Int
                while (audioRecord.read(bytes, 0, bytes.size).also { length = it } > 0) {
                    bufferedSink.write(bytes, 0, length)
                    totalLength += length

                    val minDb = -60F
                    val maxDb = 0F
                    var sum = 0.0
                    for (i in 0 until length - 1 step 2) {
                        val low: Int = bytes[i].toInt() and 0xFF // 小端
                        val high: Int = bytes[i + 1].toInt() shl 8
                        val shortValue: Short = (low or high).toShort()
                        sum += shortValue.toDouble() * shortValue.toDouble()
                    }
                    val sampleCount = length / 2
                    val rms = sqrt(sum / sampleCount)
                    val cb = if (rms > 0) 20.0F * kotlin.math.log10(rms / Short.MAX_VALUE).toFloat() else -120F
                    val percent: Float = ((cb - minDb) / (maxDb - minDb)).coerceIn(0F, 1F)
                    callback(percent)

                    Log.i(TAG, "startRecord -> sum: $sum, rms: $rms, cb: $cb")
                }
                bufferedSink.flush()
                Log.i(TAG, "startRecord -> 录制完成, 文件大小为: $totalLength bytes")
            }
    }

    private fun calcDb(samples: ShortArray, shortSize: Int): Float {
        var sum = 0.0
        for (s in samples) {
            sum += s / shortSize * s
        }
        val rms = sqrt(sum)
        // 参考值 32768，对应 0 dB
        val ref = 32768F
        return if (rms > 0) 20.0F * kotlin.math.log10(rms / ref).toFloat() else -120F
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