package edu.tyut.webviewlearn.voice

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val TAG: String = "VoiceManager"

/**
 * http://soundfile.sapp.org/doc/WaveFormat/
 * ✅ 一句话理解
 * WAV 文件 = 44 字节 WAV Header + 原始 PCM 数据
 *
 * 🧩 WAV 文件头格式
 * 字段	长度	内容
 *
 * RIFF	4 bytes	"RIFF" 字符串
 * 文件大小	4 bytes	36 + PCM 数据大小
 * WAVE	4 bytes	"WAVE" 字符串
 * fmt	4 bytes	"fmt "
 * 子块1大小	4 bytes	16（PCM）
 * 音频格式	2 bytes	1（PCM）
 * 声道数	2 bytes	1 = mono, 2 = stereo
 * 采样率	4 bytes	eg. 44100
 * 字节率	4 bytes	= 采样率 × 声道数 × 每样本字节数
 * 每帧字节数	2 bytes	= 声道数 × 每样本字节数
 * 每样本位数	2 bytes	eg. 16
 * data	4 bytes	"data"
 * 数据大小	4 bytes	PCM 数据字节数
 *
 */
@SuppressLint("MissingPermission")
internal class VoiceManager {

    private val channelMask: Int = AudioFormat.CHANNEL_IN_MONO
    private val sampleRate = 16000
    private val bufferSize: Int = AudioRecord.getMinBufferSize(sampleRate, channelMask, AudioFormat.ENCODING_PCM_16BIT)

    private val audioRecord: AudioRecord by lazy {
        // 没有权限不能进行初始化注意记住
        AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(channelMask).build())
            .setBufferSizeInBytes(bufferSize)
            .build()
    }

    internal val isRecording: Boolean get() = audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING

    internal suspend fun startRecord(context: Context, uri: Uri) = withContext(context = Dispatchers.IO) {

        val wavUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider",  File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/hello.wav"))

        audioRecord.startRecording()
        var totalLength = 0
        val bytes = ByteArray(bufferSize)
        var length: Int
        context.contentResolver.openOutputStream(uri)?.sink()?.buffer()?.use { bufferedSink: BufferedSink ->
            while (audioRecord.read(bytes, 0, bytes.size).also { length = it } > 0){
                bufferedSink.write(bytes, 0, length)
                totalLength += length
                Log.i(TAG, "startRecord -> data: ${bytes.joinToString()}")
            }
            // 写入wav头
            bufferedSink.flush()
            Log.i(TAG, "startRecord -> 录制完成...")
        }
        context.contentResolver?.openInputStream(uri)?.source()?.buffer()?.use { bufferedSource: BufferedSource ->
            context.contentResolver.openOutputStream(wavUri)?.sink()?.buffer()?.use { bufferedSink: BufferedSink ->
                val wavHeader: ByteArray = writeWavHeader(totalLength = totalLength)
                bufferedSink.write(source = wavHeader)
                bufferedSource.readAll(sink = bufferedSink)
                bufferedSink.flush()
            }
        }
    }

    private fun writeWavHeader(
        totalLength: Int,
        channels: Int = 1, // mono
        bitsPerSample: Int = 16,
    ): ByteArray {
        val header = ByteArray(44)
        val buffer: ByteBuffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))       // Chunk ID
        buffer.putInt(totalLength + 36)                             // Chunk Size
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))       // Format
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))       // Subchunk1 ID
        buffer.putInt(16)                                       // Subchunk1 Size
        buffer.putShort(1)                                      // Audio format = PCM
        buffer.putShort(1)                     // Channels
        buffer.putInt(sampleRate)                               // Sample rate
        buffer.putInt(sampleRate * 1 * bitsPerSample / 8)                                 // Byte rate
        buffer.putShort((1 * bitsPerSample / 8).toShort()) // Block align
        buffer.putShort(bitsPerSample.toShort())                // Bits per sample
        buffer.put("data".toByteArray(Charsets.US_ASCII))       // Subchunk2 ID
        buffer.putInt(totalLength)                            // Subchunk2 size
        return header
    }

    internal fun stopRecord(){
        if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING){
            audioRecord.stop()
        }
    }

    internal fun release(){
        if (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING){
            audioRecord.stop()
        }
        audioRecord.release()
    }

}