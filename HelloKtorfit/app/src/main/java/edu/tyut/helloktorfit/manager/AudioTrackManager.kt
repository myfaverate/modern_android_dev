package edu.tyut.helloktorfit.manager

import android.content.Context
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

private const val TAG: String = "AudioTrackManager"

internal class AudioTrackManager internal constructor() {

    private val channelMask: Int = AudioFormat.CHANNEL_OUT_MONO
    private val sampleRate = 16000
    private val bufferSize: Int =
        AudioTrack.getMinBufferSize(sampleRate, channelMask, AudioFormat.ENCODING_PCM_16BIT)

    private val audioTrack: AudioTrack by lazy {
        val audioTrack = AudioTrack.Builder()
            .setBufferSizeInBytes(bufferSize)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate).setChannelMask(channelMask).build()
            ).build()
        audioTrack
    }

    internal suspend  fun startPlay(context: Context, uri: Uri) = withContext(Dispatchers.IO){
        audioTrack.play()
        context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            val bytes = ByteArray(bufferSize)
            var length: Int
            while (inputStream.read(bytes, 0, bytes.size).also { length = it } > 0) {
                Log.i(TAG, "startPlay -> data: ${bytes.joinToString()}")
                val result: Int = audioTrack.write(bytes, 0, length)
                if (result < 0) {
                    break
                }
            }
        }
    }

    internal fun pause(){
        Log.i(TAG, "pause -> audioTrack.playState: ${audioTrack.playState}")
        if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            Log.i(TAG, "pause...")
            audioTrack.pause()
        }
    }
    internal fun stop(){
        audioTrack.stop()
    }
    internal fun release(){
        audioTrack.release()
    }
}