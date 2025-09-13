package edu.tyut.helloktorfit.manager

import android.content.Context
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.Uri
import android.util.Log
import edu.tyut.helloktorfit.ui.screen.SystemScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer
import kotlin.math.log10
import kotlin.math.sqrt

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
                Log.i(TAG, "startPlay -> size: ${bytes.size}, data: ${bytes.joinToString()}")
                // for (i in 0 until length - 1 step 2){
                //     val low: Int = bytes[i].toInt() and 0xFF // 小端
                //     val high: Int = bytes[i + 1].toInt() shl 8
                //     val shortValue: Int = (low or high) * 10
                //     val newShortValue = if (shortValue > Short.MAX_VALUE) {
                //         Short.MAX_VALUE.toInt()
                //     } else {
                //         if (shortValue < Short.MIN_VALUE){
                //             Short.MIN_VALUE.toInt()
                //         } else {
                //             shortValue
                //         }
                //     }
                //     bytes[i] = newShortValue.toByte()
                //     bytes[i + 1] = (newShortValue shr 8).toByte()
                // }
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
/**
 *  python3 ./repo sync -c -f --no-tags --no-clone-bundle --force-sync -j $(nproc)
 *  python3 ./repo init --depth 1 -u https://mirrors.tuna.tsinghua.edu.cn/git/AOSP/platform/manifest -b android-12.0.0_r31
 * curl https://mirrors.tuna.tsinghua.edu.cn/git/git-repo > .
 * ffplay -x 1280 -y 720 -ss 00:50:00 -ast 3 -sst 5 video4.mkv
 * ffmpeg -ss 00:50:00 -i video3.mkv -t 00:10:00 -c copy video4.mkv
 * ffplay -x 1280 -y 720 -ss 00:50:00 -ast 3 -sst 5 video3.mkv
 * ffplay -fs -ss 00:50:00 -ast 3  video3.mkv
 * ffplay -x 1280 -y 720 -sync ext .\video2.mp4
 */