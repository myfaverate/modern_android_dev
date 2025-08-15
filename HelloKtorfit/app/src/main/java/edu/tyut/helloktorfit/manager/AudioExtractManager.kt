package edu.tyut.helloktorfit.manager

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import edu.tyut.helloktorfit.route.Routes
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.math.max


private const val TAG: String = "AudioExtractManager"

/**
 * ffmpeg -i input.mp4 -vn -acodec pcm_s16le -ac 1 -ar 16000 out1.pcm
 * ffmpeg -f s16le -ar 16000 -ac 1 -i out1.pcm -c:a pcm_s16le output.wav
 */
internal class AudioExtractManager internal constructor() {

    fun extractAudioToPCMV2(videoPath: String) {
        val mediaExtractor = MediaExtractor()

        try {

            mediaExtractor.setDataSource(videoPath)

            var audioTrackIndex = -1
            var format: MediaFormat? = null

            for (i in 0 until mediaExtractor.trackCount) {
                val trackFormat: MediaFormat = mediaExtractor.getTrackFormat(i)
                val mime: String? = trackFormat.getString(MediaFormat.KEY_MIME)
                if (mime != null && mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex == -1 || format == null) {
                Log.e(TAG, "No audio track found")
                return
            }

            mediaExtractor.selectTrack(audioTrackIndex)
            val mime: String = format.getString(MediaFormat.KEY_MIME)!!
            val codec: MediaCodec = MediaCodec.createDecoderByType(mime)

            try {
                codec.configure(format, null, null, 0)
                codec.start()

                val bufferInfo = MediaCodec.BufferInfo()

                val outputFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "out1.pcm"
                )

                FileOutputStream(outputFile).use { fileOutputStream ->
                    var inIndex: Int
                    while (codec.dequeueOutputBuffer(bufferInfo, 1000).also { inIndex = it } > 0) {
                        codec.getInputBuffer(inIndex)?.let { inputBuffer: ByteBuffer ->
                            val sampleSize = mediaExtractor.readSampleData(inputBuffer, 0)

                        }

                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "extractAudioToPCMV2 -> error: ${e.message}", e)
            } finally {
                codec.stop()
                codec.release()
            }

        } catch (e: Exception) {
            Log.i(TAG, "extractAudioToPCMV2 -> error: ${e.message}", e)
        } finally {
            mediaExtractor.release()
        }
    }

    fun extractAudioToPCM(videoPath: String) {
        val extractor = MediaExtractor()
        try {

            extractor.setDataSource(videoPath)

            var audioTrackIndex = -1
            var format: MediaFormat? = null

            for (i in 0 until extractor.trackCount) {
                val trackFormat: MediaFormat = extractor.getTrackFormat(i)
                val mime: String? = trackFormat.getString(MediaFormat.KEY_MIME)
                if (mime != null && mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    format = trackFormat
                    break
                }
            }

            if (audioTrackIndex == -1 || format == null) {
                Log.e(TAG, "No audio track found")
                return
            }

            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 16000)
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1)
            format.setInteger(MediaFormat.KEY_PCM_ENCODING, AudioFormat.ENCODING_PCM_16BIT) // s16le

            extractor.selectTrack(audioTrackIndex)
            val mime: String = format.getString(MediaFormat.KEY_MIME)!!
            val codec: MediaCodec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()

            val bufferInfo = MediaCodec.BufferInfo()

            val outputFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "out1.pcm"
            )
            val outputStream = FileOutputStream(outputFile)

            var isInputEOS = false
            var isOutputEOS = false

            while (!isOutputEOS) {
                // 将视频送入解码器
                if (!isInputEOS) {
                    val inIndex: Int = codec.dequeueInputBuffer(10000)
                    if (inIndex >= 0) {
                        Log.i(TAG, "extractAudioToPCM -> inIndex: $inIndex")
                        val inputBuffer1: ByteBuffer? = codec.getInputBuffer(inIndex)
                        val sampleSize = extractor.readSampleData(inputBuffer1!!, 0)

                        if (sampleSize < 0) {
                            // 添加结尾标识
                            codec.queueInputBuffer(
                                inIndex,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            isInputEOS = true
                        } else {
                            val presentationTimeUs = extractor.sampleTime
                            codec.queueInputBuffer(inIndex, 0, sampleSize, presentationTimeUs, 0)
                            extractor.advance()
                        }
                    }
                }
                // 读取解码器器的数据
                val outIndex: Int = codec.dequeueOutputBuffer(bufferInfo, 10000)
                if (outIndex >= 0) {
                    Log.i(TAG, "extractAudioToPCM -> outIndex: $outIndex")
                    // val outputBuffer = outputBuffers[outIndex]
                    val outputBuffer: ByteBuffer = codec.getOutputBuffer(outIndex)!!
                    val pcmChunk = ByteArray(bufferInfo.size)
                    outputBuffer.get(pcmChunk)
                    outputBuffer.clear()
                    outputStream.write(pcmChunk)

                    codec.releaseOutputBuffer(outIndex, false)

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        isOutputEOS = true
                    }
                }
            }
            outputStream.close()
            codec.stop()
            codec.release()
        } catch (e: Exception) {
            Log.e(TAG, "extractAudioToPCM -> error: ${e.message}", e)
        } finally {
            extractor.release()
        }
    }

    /**
     * 视频文件绝对路径
     */
    @SuppressLint("WrongConstant")
    internal fun extractAudioFromVideo(videoPath: String) {
        val mediaExtract = MediaExtractor()
        val audio = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "out1.mp4"
        )
        val mediaMuxer = MediaMuxer(audio.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        try {
            mediaExtract.setDataSource(videoPath)
            val trackCount: Int = mediaExtract.trackCount
            var maxInputSize = 0
            Log.i(TAG, "extractAudioFromVideo -> trackCount: $trackCount")
            for (i in 0 until trackCount) {
                val trackFormat: MediaFormat = mediaExtract.getTrackFormat(i)
                Log.i(
                    TAG,
                    "extractAudioFromVideo -> all: ${trackFormat.getString(MediaFormat.KEY_MIME)}"
                )
                if (this.isAudioTack(trackFormat)) {
                    maxInputSize =
                        max(maxInputSize, trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE))
                    mediaExtract.selectTrack(i)
                    mediaMuxer.addTrack(trackFormat)
                    break
                }
            }
            mediaMuxer.start()
            Log.i(TAG, "extractAudioFromVideo2 -> maxInputSize: $maxInputSize")
            val buffer: ByteBuffer = ByteBuffer.allocate(maxInputSize)
            val bufferInfo = MediaCodec.BufferInfo()
            var simpleSize: Int
            while (mediaExtract.readSampleData(buffer, 0).also { simpleSize = it } > 0) {
                bufferInfo.size = simpleSize
                bufferInfo.presentationTimeUs = mediaExtract.sampleTime
                bufferInfo.offset = 0
                bufferInfo.flags = mediaExtract.sampleFlags
                mediaMuxer.writeSampleData(0, buffer, bufferInfo)
                mediaExtract.advance()
                Log.i(TAG, "extractAudioFromVideo2 -> bufferInfo: $bufferInfo")
            }
        } catch (e: Exception) {
            Log.e(TAG, "extractAudioFromVideo error: ${e.message}", e)
        } finally {
            mediaExtract.release()
            mediaMuxer.release()
        }
    }

    internal fun extractAudioFromVideo1(videoPath: String) {
        val mediaExtract = MediaExtractor()
        val mediaCodec: MediaCodec = MediaCodec.createDecoderByType("audio/mp4a-latm")
        var audioTrackIndex = -1
        try {
            mediaExtract.setDataSource(videoPath)
            val trackCount: Int = mediaExtract.trackCount
            Log.i(TAG, "extractAudioFromVideo -> trackCount: $trackCount")
            for (i in 0 until trackCount) {
                val trackFormat: MediaFormat = mediaExtract.getTrackFormat(i)
                Log.i(
                    TAG,
                    "extractAudioFromVideo -> all: ${trackFormat.getString(MediaFormat.KEY_MIME)}"
                )
                if (this.isAudioTack(trackFormat)) {
                    audioTrackIndex = i
                    val maxInputSize: Int = trackFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
                    Log.i(TAG, "extractAudioFromVideo -> maxInputSize: $maxInputSize")
                    break
                }
            }
            if (audioTrackIndex == -1) {
                Log.i(TAG, "extractAudioFromVideo -> No audio track found in $videoPath")
                return
            }
            mediaExtract.selectTrack(audioTrackIndex)

            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "out.aac"
            ).outputStream().use { outputStream ->
                val bufferSize = 1024 * 8
                val buffer: ByteBuffer = ByteBuffer.allocate(bufferSize)
                var length: Int
                while (mediaExtract.readSampleData(buffer, 0).also { length = it } > 0) {
                    outputStream.write(buffer.array(), 0, length)
                    buffer.clear()
                    mediaExtract.advance()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "extractAudioFromVideo error: ${e.message}", e)
        } finally {
            mediaExtract.release()
        }
    }

    private fun isAudioTack(mediaFormat: MediaFormat): Boolean {
        val mime: String? = mediaFormat.getString(MediaFormat.KEY_MIME)
        Log.i(TAG, "isAudioTack -> mime: $mime")
        return mime?.startsWith(prefix = "audio/") ?: false
    }

    internal fun pcmToAac() {
        // 编码器
        // aac 3gpp audio/amr-wb  audio/flac
        // h264
        // 解码器
        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        // mediaCodecList.codecInfos.filter { it.isEncoder }.forEach { mediaCodecInfo ->
        //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //         Log.i(TAG, "pcmToAac -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
        //     } else {
        //         Log.i(TAG, "pcmToAac -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
        //     }
        // }
        val mp3Encoders: List<MediaCodecInfo> = mediaCodecList.codecInfos.filter {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isHardwareAccelerated && it.supportedTypes.contains(element = MediaFormat.MIMETYPE_AUDIO_MPEG)
            } else {
                it.supportedTypes.contains(element = MediaFormat.MIMETYPE_AUDIO_MPEG)
            }
        }
        Log.i(TAG, "pcmToAac -> mp3Encoders: ${mp3Encoders.joinToString { it.name }}")
        if (mp3Encoders.isEmpty()){
            // 不支持硬件解码
            Log.i(TAG, "pcmToAac -> 不支持硬件解码...")
            return
        }
        val mediaFormat: MediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_MPEG, 16000, 1)
        val mediaCodec: MediaCodec = MediaCodec.createEncoderByType(mp3Encoders[0].name)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        // TODO 异步编码
    }
}