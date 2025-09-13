package edu.tyut.helloktorfit.manager

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.internal.notify
import okhttp3.internal.wait
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread
import kotlin.coroutines.resume
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

    internal fun extractAudioFromVideo1(videoPath: String): Unit {
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

    internal suspend fun pcmToMp3(
        context: Context,
        pcmUri: Uri,
        mp3Uri: Uri,
    ): Unit = suspendCancellableCoroutine { continuation ->

        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        val mp3Encoders: List<MediaCodecInfo> = mediaCodecList.codecInfos.filter { it.isEncoder }.filter {
            it.supportedTypes.contains(element = MediaFormat.MIMETYPE_AUDIO_MPEG)
        }
        if (mp3Encoders.isEmpty()){
            if (continuation.isActive){
                continuation.resume(Unit) // 不支持的话可以使用lame软编码
            }
            return@suspendCancellableCoroutine
        }

        mp3Encoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "pcmToAac -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "pcmToAac -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
        }

        val mediaFormat: MediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_MPEG, 16000, 1).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        }

        val mediaCodec: MediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_MPEG)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        val pcmInputStream: InputStream = context.contentResolver.openInputStream(pcmUri)!!
        val mp3OutputStream: OutputStream = context.contentResolver.openOutputStream(mp3Uri)!!
        val bytes = ByteArray(1024 * 8)

        mediaCodec.setCallback(object : MediaCodec.Callback(){
            override fun onError(
                codec: MediaCodec,
                e: MediaCodec.CodecException
            ) {
                Log.e(TAG, "onError name: ${codec.name}, thread: ${Thread.currentThread()}, error: ${e.message}", e)
            }

            override fun onInputBufferAvailable(
                codec: MediaCodec,
                index: Int
            ) {
                val inputBuffer: ByteBuffer = codec.getInputBuffer(index) ?: return
                val size: Int = pcmInputStream.read(bytes, 0, inputBuffer.limit()) // 或许需要去 min(bytes.size, inputBuffer.limit())
                Log.i(TAG, "onInputBufferAvailable -> name: ${mediaCodec.name}, index: $index, thread: ${Thread.currentThread()}, size: $size, limit: ${inputBuffer.limit()}, position: ${inputBuffer.position()}")
                if (size > 0) {
                    inputBuffer.put(bytes, 0, size)
                    codec.queueInputBuffer(index, 0, size, System.nanoTime() / 1000, 0)
                } else {
                    codec.queueInputBuffer(index, 0, 0, System.nanoTime() / 1000, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                Log.i(TAG, "onOutputBufferAvailable -> name: ${codec.name}, index: $index, info: ${info.size}, thread: ${Thread.currentThread()}")
                val outputBuffer: ByteBuffer = codec.getOutputBuffer(index) ?: return

                outputBuffer.get(bytes, 0, info.size)
                mp3OutputStream.write(bytes, 0, info.size)

                codec.releaseOutputBuffer(index, false)
                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM){
                    Log.i(TAG, "onOutputBufferAvailable -> == 编码结束...") // todo
                    pcmInputStream.close()
                    mp3OutputStream.close()
                    if (continuation.isActive){
                        Log.i(TAG, "pcmToMp3 -> 解码完成 resume before...")
                        continuation.resume(Unit)
                        Log.i(TAG, "pcmToMp3 -> 解码完成 resume after...")
                    }
                }
            }

            override fun onOutputFormatChanged(
                codec: MediaCodec,
                format: MediaFormat
            ) {
                Log.i(TAG, "onOutputFormatChanged -> name: ${codec.name}, format: ${format.getString(MediaFormat.KEY_MIME)}")
            }
        })

        Log.i(TAG, "pcmToAac -> before start...")
        mediaCodec.start()
        Log.i(TAG, "pcmToAac -> after start...")
    }

    internal suspend fun pcmToAac(context: Context, pcmUri: Uri, aacUri: Uri): Unit = suspendCancellableCoroutine { continuation ->

        // 编码器
        // aac 3gpp audio/amr-wb  audio/flac
        // h264
        // 解码器 解码器那可就太多了

        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)

        val aacEncoders: List<MediaCodecInfo> = mediaCodecList.codecInfos.filter { it.isEncoder }.filter {
            it.supportedTypes.contains(element = MediaFormat.MIMETYPE_AUDIO_AAC)
        }

        aacEncoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "pcmToAac -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "pcmToAac -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
        }


        Log.i(TAG, "pcmToAac -> aacEncoders: ${aacEncoders.joinToString { it.name }}")

        if (aacEncoders.isEmpty()){
            // 不支持硬件解码
            Log.i(TAG, "pcmToAac -> 不支持硬件编码...") // 使用软解码
            if (continuation.isActive){
                continuation.resume(Unit)
            }
            return@suspendCancellableCoroutine
        }

        // 可惜 aac 仅支持软解码
        Log.i(TAG, "pcmToAac -> 支持硬件编码")
        val mediaFormat: MediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 16000, 1).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, 128000)
        }

        val mediaCodec: MediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        val pcmInputStream: InputStream = context.contentResolver.openInputStream(pcmUri)!!
        val aacOutputStream: OutputStream = context.contentResolver.openOutputStream(aacUri)!!
        val bytes = ByteArray(1024 * 8)

        mediaCodec.setCallback(object : MediaCodec.Callback() {
            override fun onError(
                codec: MediaCodec,
                e: MediaCodec.CodecException
            ) {
                Log.e(
                    TAG,
                    "onError name: ${codec.name}, thread: ${Thread.currentThread()}, error: ${e.message}",
                    e
                )
            }

            override fun onInputBufferAvailable(
                codec: MediaCodec,
                index: Int
            ) {
                Log.i(
                    TAG,
                    "onInputBufferAvailable -> name: ${mediaCodec.name}, index: $index, thread: ${Thread.currentThread()}"
                )
                val inputBuffer: ByteBuffer = codec.getInputBuffer(index) ?: return
                val size: Int = pcmInputStream.read(bytes, 0, inputBuffer.limit())
                if (size > 0) {
                    inputBuffer.put(bytes, 0, size)
                    codec.queueInputBuffer(index, 0, size, System.nanoTime() / 1000, 0)
                } else {
                    codec.queueInputBuffer(
                        index,
                        0,
                        0,
                        System.nanoTime() / 1000,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                Log.i(
                    TAG,
                    "onOutputBufferAvailable -> name: ${codec.name}, index: $index, info: ${info.size}, thread: ${Thread.currentThread()}"
                )

                val outputBuffer: ByteBuffer = codec.getOutputBuffer(index) ?: return

                val aacData = ByteArray(info.size + 7)
                addAdtsHeader(aacData, aacData.size, 16000, 1)
                outputBuffer.get(aacData, 7, info.size)

                aacOutputStream.write(aacData)

                codec.releaseOutputBuffer(index, false)

                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i(TAG, "onOutputBufferAvailable -> == 编码结束...") // todo
                    pcmInputStream.close()
                    aacOutputStream.close()
                    if (continuation.isActive) {
                        Log.i(TAG, "pcmToAac -> 解码完成 resume before...")
                        continuation.resume(Unit)
                        Log.i(TAG, "pcmToAac -> 解码完成 resume after...")
                    }
                }
            }

            override fun onOutputFormatChanged(
                codec: MediaCodec,
                format: MediaFormat
            ) {
                Log.i(
                    TAG,
                    "onOutputFormatChanged -> name: ${codec.name}, format: ${
                        format.getString(MediaFormat.KEY_MIME)
                    }"
                )
            }
        })
        Log.i(TAG, "pcmToAac -> before start...")
        mediaCodec.start()
        Log.i(TAG, "pcmToAac -> after start...")
    }



    @Suppress("SameParameterValue")
    private fun addAdtsHeader(packet: ByteArray, packetLen: Int, sampleRate: Int, channels: Int) {
        val profile = 2  // AAC LC
        val freqIdx = when(sampleRate){
            96000 -> 0
            88200 -> 1
            64000 -> 2
            48000 -> 3
            44100 -> 4
            32000 -> 5
            24000 -> 6
            22050 -> 7
            16000 -> 8
            12000 -> 9
            11025 -> 10
            8000 -> 11
            else -> 4 // 默认44100
        }

        /**
         0000 0x00
         0001 0x01
         0010 0x02
         0011 0x03
         0100 0x04
         0101 0x05
         0110 0x06
         0111 0x07
         1000 0x08
         1001 0x09
         1010 0x0A
         1011 0x0B
         1100 0x0C
         1101 0x0D
         1110 0x0E
         1111 0x0F
         */
        val chanCfg = channels // CPE = 1, mono = 1
        packet[0] = 0xFF.toByte() // 1111 1111 1字节
        packet[1] = 0xF9.toByte() // 1111 1001 2字节 id 1 for MPEG-2, layer = 00 protection_absent = 1

        // [01]00 0000 profile 2bits
        // [0110  00]00 freqIdx 4bits
        // [0100  000]0 Private bit

        // channel 3 bits 001 右移两位 0

        // [0100 0000] channel的1位0加上来 // 3字节

        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()


        // 0000 0000

        // channel 0000 0001 左移6位 01

        // [01]00 0000 channel 处理完成 // 26

        //  Originality、Home两bit Copyright ID bit, 	Copyright ID start 2bit 合并到 packetLen，所有其一共17bits

                                    // 0x7FFF
        // 0000 0000 0000 000[0 00011|111 1111 1111] packetLen 右边移 11 ->
        // 0000 0000 0000 0000 0000 00[00 0011] packetLen 右边移 11 ->

        // [0100 0011]

        packet[3] = (((chanCfg and 3) shl 6) + (packetLen shr 11)).toByte() // 4字节

        packet[4] = (packetLen and 0x7FF shr 3).toByte() // [111 1111 1]111

        // [111]0 0000 后面即 0xFFC

        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte() // 0001 1111

        // 1111 1100
        packet[6] = 0xFC.toByte() // 7字节
    }

    /**
     * 解码 ogg 为 pcm
     */
    internal suspend fun oggToPcm(context: Context, oggUri: Uri, pcmUri: Uri): Unit = suspendCancellableCoroutine { continuation ->

        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        // 解码器
        val oggDecoders: List<MediaCodecInfo> = mediaCodecList.codecInfos.filter { !it.isEncoder }.filter {
            it.supportedTypes.contains(element = MediaFormat.MIMETYPE_AUDIO_VORBIS)
        }

        oggDecoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "oggToPcm -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "oggToPcm -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
        }

        Log.i(TAG, "pcmToAac -> oggDecoders: ${oggDecoders.joinToString { it.name }}")
        if (oggDecoders.isEmpty()){
            // 不支持硬件解码
            Log.i(TAG, "oggToPcm -> 不支持硬件解码...") // 使用软解码
            if (continuation.isActive){
                continuation.resume(Unit)
            }
            return@suspendCancellableCoroutine
        }

        // 可惜 aac 仅支持软解码
        Log.i(TAG, "oggToPcm -> 支持硬件解码")

        val mediaFormat1: MediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_RAW, 16000, 1)

        val mediaExtractor: MediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(context, oggUri, mapOf<String, String>())

        val mediaFormat2: MediaFormat =  mediaExtractor.getTrackFormat(0) // 或者提前判断trackCount > 0

        Log.i(TAG, "oggToPcm -> mediaFormat1: $mediaFormat1, mediaFormat2: $mediaFormat2")

        mediaExtractor.selectTrack(0)
        val mediaCodec: MediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_VORBIS)
        mediaCodec.configure(mediaFormat2, null, null, 0) // 解码

        val pcmInputStream: InputStream = context.contentResolver.openInputStream(oggUri)!!
        val aacOutputStream: OutputStream = context.contentResolver.openOutputStream(pcmUri)!!
        val bytes = ByteArray(1024 * 8)

        mediaCodec.setCallback(object : MediaCodec.Callback() {
            override fun onError(
                codec: MediaCodec,
                e: MediaCodec.CodecException
            ) {
                Log.e(
                    TAG,
                    "onError name: ${codec.name}, thread: ${Thread.currentThread()}, error: ${e.message}",
                    e
                )
            }

            override fun onInputBufferAvailable(
                codec: MediaCodec,
                index: Int
            ) {
                val inputBuffer: ByteBuffer = codec.getInputBuffer(index) ?: return
                val size: Int = mediaExtractor.readSampleData(inputBuffer, 0)
                Log.i(
                    TAG,
                    "onInputBufferAvailable -> name: ${mediaCodec.name}, index: $index, thread: ${Thread.currentThread()}, size: $size"
                )
                if (size > 0) {
                    codec.queueInputBuffer(index, 0, size, mediaExtractor.sampleTime, mediaExtractor.sampleFlags)
                    mediaExtractor.advance()
                } else {
                    codec.queueInputBuffer(
                        index,
                        0,
                        0,
                        0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                Log.i(
                    TAG,
                    "onOutputBufferAvailable -> name: ${codec.name}, index: $index, infoSize: ${info.size}, thread: ${Thread.currentThread()}"
                )

                val outputBuffer: ByteBuffer = codec.getOutputBuffer(index) ?: return

                outputBuffer.get(bytes, 0, info.size)

                aacOutputStream.write(bytes, 0, info.size)

                codec.releaseOutputBuffer(index, false)

                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i(TAG, "onOutputBufferAvailable -> == 编码结束...") // todo
                    pcmInputStream.close()
                    aacOutputStream.close()
                    mediaExtractor.release()
                    if (continuation.isActive) {
                        Log.i(TAG, "pcmToAac -> 解码完成 resume before...")
                        continuation.resume(Unit)
                        Log.i(TAG, "pcmToAac -> 解码完成 resume after...")
                    }
                }
            }

            override fun onOutputFormatChanged(
                codec: MediaCodec,
                format: MediaFormat
            ) {
                Log.i(
                    TAG,
                    "onOutputFormatChanged -> name: ${codec.name}, format: $format"
                )
            }
        })
        Log.i(TAG, "pcmToAac -> before start...")
        mediaCodec.start()
        Log.i(TAG, "pcmToAac -> after start...")
    }

    /**
     * 解码 ogg 为 pcm
     */
    internal suspend fun flacToPcm(context: Context, flacUri: Uri, pcmUri: Uri): Unit = suspendCancellableCoroutine { continuation ->

        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)
        // 解码器
        val flacDecoders: List<MediaCodecInfo> = mediaCodecList.codecInfos.filter { !it.isEncoder }.filter {
            it.supportedTypes.contains(element = MediaFormat.MIMETYPE_AUDIO_FLAC)
        }

        flacDecoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "oggToPcm -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "oggToPcm -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
        }

        Log.i(TAG, "pcmToAac -> oggDecoders: ${flacDecoders.joinToString { it.name }}")
        if (flacDecoders.isEmpty()){
            // 不支持硬件解码
            Log.i(TAG, "oggToPcm -> 不支持硬件解码...") // 使用软解码
            if (continuation.isActive){
                continuation.resume(Unit)
            }
            return@suspendCancellableCoroutine
        }

        // 可惜 aac 仅支持软解码
        Log.i(TAG, "oggToPcm -> 支持硬件解码")


        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(context, flacUri, mapOf<String, String>())

        val mediaFormat2: MediaFormat =  mediaExtractor.getTrackFormat(0) // 或者提前判断trackCount > 0
        for (i in 0 until mediaExtractor.trackCount){
            Log.i(TAG, "flacToPcm -> format: ${mediaExtractor.getTrackFormat(i)}")
        }

        Log.i(TAG, "oggToPcm -> mediaFormat2: $mediaFormat2, trackCount: ${mediaExtractor.trackCount}")

        mediaExtractor.selectTrack(0)
        val mediaCodec: MediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_RAW)
        mediaCodec.configure(mediaFormat2, null, null, 0) // 解码

        val aacOutputStream: OutputStream = context.contentResolver.openOutputStream(pcmUri)!!
        val bytes = ByteArray(1024 * 1024)

        mediaCodec.setCallback(object : MediaCodec.Callback() {
            override fun onError(
                codec: MediaCodec,
                e: MediaCodec.CodecException
            ) {
                Log.e(
                    TAG,
                    "onError name: ${codec.name}, thread: ${Thread.currentThread()}, error: ${e.message}",
                    e
                )
            }

            override fun onInputBufferAvailable(
                codec: MediaCodec,
                index: Int
            ) {
                val inputBuffer: ByteBuffer = codec.getInputBuffer(index) ?: return
                val size: Int = mediaExtractor.readSampleData(inputBuffer, 0)
                Log.i(
                    TAG,
                    "onInputBufferAvailable -> name: ${mediaCodec.name}, index: $index, thread: ${Thread.currentThread()}, size: $size"
                )
                if (size > 0) {
                    codec.queueInputBuffer(index, 0, size, mediaExtractor.sampleTime, mediaExtractor.sampleFlags)
                    mediaExtractor.advance()
                } else {
                    codec.queueInputBuffer(
                        index,
                        0,
                        0,
                        0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                Log.i(
                    TAG,
                    "onOutputBufferAvailable -> name: ${codec.name}, index: $index, infoSize: ${info.size}, thread: ${Thread.currentThread()}"
                )

                val outputBuffer: ByteBuffer = codec.getOutputBuffer(index) ?: return

                outputBuffer.get(bytes, 0, info.size)

                aacOutputStream.write(bytes, 0, info.size)

                codec.releaseOutputBuffer(index, false)

                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i(TAG, "onOutputBufferAvailable -> == 编码结束...") // todo
                    aacOutputStream.close()
                    mediaExtractor.release()
                    if (continuation.isActive) {
                        Log.i(TAG, "pcmToAac -> 解码完成 resume before...")
                        continuation.resume(Unit)
                        Log.i(TAG, "pcmToAac -> 解码完成 resume after...")
                    }
                }
            }

            override fun onOutputFormatChanged(
                codec: MediaCodec,
                format: MediaFormat
            ) {
                Log.i(
                    TAG,
                    "onOutputFormatChanged -> name: ${codec.name}, format: $format"
                )
            }
        })
        Log.i(TAG, "pcmToAac -> before start...")
        mediaCodec.start()
        Log.i(TAG, "pcmToAac -> after start...")
    }


    internal fun oggToPcm1(context: Context, oggUri: Uri, pcmUri: Uri) : Unit  {
        Log.i(TAG, "oggToPcm1 -> launch before: ${Thread.currentThread()}")
        var done = false
        thread {
            Thread.sleep(1000)
            synchronized(this@AudioExtractManager){
                Log.i(TAG, "oggToPcm1 -> notify before: ${Thread.currentThread()}")
                done = true
                this@AudioExtractManager.notify()
                Log.i(TAG, "oggToPcm1 -> notify after: ${Thread.currentThread()}")
            }
        }
        Log.i(TAG, "oggToPcm1 -> launch after: ${Thread.currentThread()}")
        Thread.sleep(2000)
        synchronized(this@AudioExtractManager){
            Log.i(TAG, "oggToPcm1 -> wait before: ${Thread.currentThread()}")
            while (!done) {
                this@AudioExtractManager.wait()
            }
            Log.i(TAG, "oggToPcm1 -> wait after: ${Thread.currentThread()}")
        }
        Log.i(TAG, "oggToPcm1 -> finish...")
    }

    // ffmpeg -i video.m4s -i audio.m4s -c copy output.mp4
    internal suspend fun videoToYuvPcm(context: Context, videoUri: Uri, yuvUri: Uri, pcmUri: Uri): Unit {

        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)

        // 视频解码器材
        val h264Decoders: List<MediaCodecInfo> = mediaCodecList.codecInfos
            .filter { !it.isEncoder && MediaFormat.MIMETYPE_VIDEO_AVC in it.supportedTypes }

        if (h264Decoders.isEmpty()){
            Log.i(TAG, "videoToYuvPcm -> 不支持h264解码")
            return
        }

        // 拿解码器
        val h264Decoder: MediaCodecInfo = h264Decoders.firstOrNull {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isHardwareAccelerated
            } else {
                true
            }
        } ?: h264Decoders.first()

        // MediaCodec.createByCodecName(h264Decoder.name)

        h264Decoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "h264Decoders -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "h264Decoders -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
        }

        // 音频解码器
        val aacDecoders: List<MediaCodecInfo> = mediaCodecList.codecInfos.filter { !it.isEncoder }.filter {
            it.supportedTypes.contains(element = MediaFormat.MIMETYPE_AUDIO_AAC)
        }

        if (aacDecoders.isEmpty()){
            Log.i(TAG, "videoToYuvPcm -> 不支持aac解码")
            return
        }

        val aacDecoder: MediaCodecInfo = aacDecoders.firstOrNull {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isHardwareAccelerated
            } else {
                true
            }
        } ?: aacDecoders.first()

        aacDecoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "aacDecoders -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "aacDecoders -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
        }

        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(context, videoUri, mapOf<String, String>())

        for (i in 0 until mediaExtractor.trackCount){
            Log.i(TAG, "flacToPcm -> format: ${mediaExtractor.getTrackFormat(i)}")
        }

        if (mediaExtractor.trackCount < 2){
            mediaExtractor.release()
            return
        }

        Log.i(TAG, "videoToYuvPcm -> decode before...")
        //
        decode(context, 0, yuvUri, mediaExtractor, h264Decoder.name)
        // ffplay -f s16le -ar 44100 -ch_layout stereo -i output.pcm
        decode(context, 1, pcmUri, mediaExtractor, aacDecoder.name)
        Log.i(TAG, "videoToYuvPcm -> decode after...")

        mediaExtractor.release()

        Log.i(TAG, "videoToYuvPcm -> end...")
    }

    private suspend fun decode(
        context: Context,
        index: Int,
        output: Uri,
        mediaExtractor: MediaExtractor,
        decodeName: String
    ): Unit = suspendCancellableCoroutine { continuation ->

        val mediaFormat: MediaFormat = mediaExtractor.getTrackFormat(index)
        mediaExtractor.selectTrack(index)
        val mediaCodec: MediaCodec = MediaCodec.createByCodecName(decodeName)
        mediaCodec.configure(mediaFormat, null, null, 0)

        val aacOutputStream: OutputStream = context.contentResolver.openOutputStream(output)!!
        val bytes = ByteArray(1024 * 1024 * 100)

        mediaCodec.setCallback(object : MediaCodec.Callback() {
            override fun onError(
                codec: MediaCodec,
                e: MediaCodec.CodecException
            ) {
                Log.e(
                    TAG,
                    "onError name: ${codec.name}, thread: ${Thread.currentThread()}, error: ${e.message}",
                    e
                )
            }

            override fun onInputBufferAvailable(
                codec: MediaCodec,
                index: Int
            ) {
                val inputBuffer: ByteBuffer = codec.getInputBuffer(index) ?: return
                val size: Int = mediaExtractor.readSampleData(inputBuffer, 0)
                Log.i(
                    TAG,
                    "onInputBufferAvailable -> name: ${mediaCodec.name}, index: $index, thread: ${Thread.currentThread()}, size: $size"
                )
                if (size > 0) {
                    codec.queueInputBuffer(index, 0, size, mediaExtractor.sampleTime, mediaExtractor.sampleFlags)
                    mediaExtractor.advance()
                } else {
                    codec.queueInputBuffer(
                        index,
                        0,
                        0,
                        0,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                    Log.i(TAG, "onInputBufferAvailable -> BUFFER_FLAG_END_OF_STREAM")
                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                Log.i(
                    TAG,
                    "onOutputBufferAvailable -> name: ${codec.name}, index: $index, infoSize: ${info.size}, thread: ${Thread.currentThread()}"
                )

                val outputBuffer: ByteBuffer = codec.getOutputBuffer(index) ?: return

                outputBuffer.get(bytes, 0, info.size)

                aacOutputStream.write(bytes, 0, info.size)

                codec.releaseOutputBuffer(index, false)

                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i(TAG, "onOutputBufferAvailable -> == 编码结束...") // todo
                    aacOutputStream.close()
                    if (continuation.isActive) {
                        Log.i(TAG, "pcmToAac -> 解码完成 resume before...")
                        continuation.resume(Unit)
                        Log.i(TAG, "pcmToAac -> 解码完成 resume after...")
                    }
                }
            }

            override fun onOutputFormatChanged(
                codec: MediaCodec,
                format: MediaFormat
            ) {
                Log.i(
                    TAG,
                    "onOutputFormatChanged -> name: ${codec.name}, format: $format"
                )
            }
        })
        Log.i(TAG, "pcmToAac -> before start...")
        mediaCodec.start()
        Log.i(TAG, "pcmToAac -> after start...")
    }

    internal suspend fun videoToYuvPcm1(context: Context, videoUri: Uri, yuvUri: Uri): Unit {

        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)

        // 视频解码器材
        val mkvDecoders: List<MediaCodecInfo> = mediaCodecList.codecInfos
            .filter { !it.isEncoder && MediaFormat.MIMETYPE_VIDEO_HEVC in it.supportedTypes }

        if (mkvDecoders.isEmpty()){
            Log.i(TAG, "videoToYuvPcm -> 不支持mkv解码")
            return
        }

        // 拿解码器
        val mkvDecoder: MediaCodecInfo = mkvDecoders.firstOrNull {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isHardwareAccelerated
            } else {
                true
            }
        } ?: mkvDecoders.first()

        // 重试解码器 android 8 不支持  hevc (Main 10)
        // val mkvDecoder: MediaCodecInfo = mkvDecoders[2]
        // MediaCodec.createByCodecName(h264Decoder.name)
        Log.i(TAG, "videoToYuvPcm1 -> mkvDecoderName: ${mkvDecoder.name}")

        mkvDecoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "mkvDecoders -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "mkvDecoders -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
        }

        // 音频解码器
        val aacDecoders: List<MediaCodecInfo> = mediaCodecList.codecInfos.filter { !it.isEncoder }.filter {
            it.supportedTypes.contains(element = MediaFormat.MIMETYPE_AUDIO_AAC)
        }

        if (aacDecoders.isEmpty()){
            Log.i(TAG, "videoToYuvPcm -> 不支持aac解码")
            return
        }

        val aacDecoder: MediaCodecInfo = aacDecoders.firstOrNull {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isHardwareAccelerated
            } else {
                true
            }
        } ?: aacDecoders.first()

        aacDecoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "aacDecoders -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "aacDecoders -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
        }

        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(context, videoUri, mapOf<String, String>())

        for (i in 0 until mediaExtractor.trackCount){
            Log.i(TAG, "flacToPcm -> format: ${mediaExtractor.getTrackFormat(i)}")
        }

        if (mediaExtractor.trackCount < 1){
            mediaExtractor.release()
            return
        }

        Log.i(TAG, "videoToYuvPcm -> decode before...")
        //
        decode(context, 0, yuvUri, mediaExtractor, mkvDecoder.name)
        // ffplay -f s16le -ar 44100 -ch_layout stereo -i output.pcm
        Log.i(TAG, "videoToYuvPcm -> decode after...")

        mediaExtractor.release()

        Log.i(TAG, "videoToYuvPcm -> end...")
    }

    internal suspend fun h265ToYuvPcm(context: Context, videoUri: Uri, yuvUri: Uri, pcmUri: Uri){

        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)

        // 视频解码器材
        val mkvDecoders: List<MediaCodecInfo> = mediaCodecList.codecInfos
            .filter { !it.isEncoder && MediaFormat.MIMETYPE_VIDEO_HEVC in it.supportedTypes }

        if (mkvDecoders.isEmpty()){
            Log.i(TAG, "videoToYuvPcm -> 不支持mkv解码")
            return
        }

        // 拿解码器
        val mkvDecoder: MediaCodecInfo = mkvDecoders.firstOrNull {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isHardwareAccelerated
            } else {
                true
            }
        } ?: mkvDecoders.first()

        // 重试解码器 android 8 不支持  hevc (Main 10)
        // val mkvDecoder: MediaCodecInfo = mkvDecoders[1]
        // MediaCodec.createByCodecName(h264Decoder.name)
        Log.i(TAG, "videoToYuvPcm1 -> mkvDecoderName: ${mkvDecoder.name}")

        mkvDecoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "mkvDecoders -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "mkvDecoders -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
            mediaCodecInfo.supportedTypes.forEach { mimeType: String ->
                if (mimeType.lowercase() == MediaFormat.MIMETYPE_VIDEO_HEVC){
                    val mediaCodecInfoCodecCapabilities: MediaCodecInfo.CodecCapabilities = mediaCodecInfo.getCapabilitiesForType(mimeType)
                    mediaCodecInfoCodecCapabilities.profileLevels.forEach { codecProfileLevel ->
                        if (codecProfileLevel.profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain){
                            if (codecProfileLevel.level >= MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel62){
                                Log.i(TAG, "h265ToYuvPcm -> 支持 8k h265 name: ${mediaCodecInfo.name}")
                            } else {
                                Log.i(TAG, "h265ToYuvPcm -> 不支持 8k h265 name: ${mediaCodecInfo.name}")
                            }
                        }
                    }
                }
            }
        }

        // 音频解码器
        val aacDecoders: List<MediaCodecInfo> = mediaCodecList.codecInfos.filter { !it.isEncoder }.filter {
            it.supportedTypes.contains(element = MediaFormat.MIMETYPE_AUDIO_AAC)
        }

        if (aacDecoders.isEmpty()){
            Log.i(TAG, "videoToYuvPcm -> 不支持aac解码")
            return
        }

        val aacDecoder: MediaCodecInfo = aacDecoders.firstOrNull {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isHardwareAccelerated
            } else {
                true
            }
        } ?: aacDecoders.first()

        aacDecoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "aacDecoders -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "aacDecoders -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
        }

        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(context, videoUri, mapOf<String, String>())

        for (i in 0 until mediaExtractor.trackCount){
            Log.i(TAG, "flacToPcm -> format: ${mediaExtractor.getTrackFormat(i)}")
        }

        if (mediaExtractor.trackCount < 1){
            mediaExtractor.release()
            return
        }

        Log.i(TAG, "videoToYuvPcm -> decode before...")
        //
        decode(context, 0, yuvUri, mediaExtractor, mkvDecoder.name)
        decode(context, 1, pcmUri, mediaExtractor, aacDecoder.name)
        // ffplay -f s16le -ar 44100 -ch_layout stereo -i output.pcm
        Log.i(TAG, "videoToYuvPcm -> decode after...")

        mediaExtractor.release()

        Log.i(TAG, "videoToYuvPcm -> end...")
    }

    //  ffplay -x 1280 -y 720 -f rawvideo -pixel_format yuv420p -video_size 3840x2176 -framerate 60 output1.yuv
    internal suspend fun yuvToh264(context: Context, yuvUri: Uri, h264Uri: Uri): Unit = suspendCancellableCoroutine{ continuation ->

        val mediaCodecList = MediaCodecList(MediaCodecList.ALL_CODECS)

        // 视频解码器材
        val mkvDecoders: List<MediaCodecInfo> = mediaCodecList.codecInfos
            .filter { !it.isEncoder && MediaFormat.MIMETYPE_VIDEO_HEVC in it.supportedTypes }

        if (mkvDecoders.isEmpty()){
            Log.i(TAG, "videoToYuvPcm -> 不支持mkv解码")
            if (continuation.isActive){
                continuation.resume(Unit)
            }
            return@suspendCancellableCoroutine
        }

        // 拿解码器
        val mkvDecoder: MediaCodecInfo = mkvDecoders.firstOrNull {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.isHardwareAccelerated
            } else {
                true
            }
        } ?: mkvDecoders.first()

        // 重试解码器 android 8 不支持  hevc (Main 10)
        // val mkvDecoder: MediaCodecInfo = mkvDecoders[1]
        // MediaCodec.createByCodecName(h264Decoder.name)
        Log.i(TAG, "videoToYuvPcm1 -> mkvDecoderName: ${mkvDecoder.name}")

        mkvDecoders.forEach { mediaCodecInfo ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Log.i(TAG, "mkvDecoders -> name: ${mediaCodecInfo.name}, canonicalName: ${mediaCodecInfo.canonicalName}, isAlias: ${mediaCodecInfo.isAlias}, isVendor: ${mediaCodecInfo.isVendor}, isHardwareAccelerated: ${mediaCodecInfo.isHardwareAccelerated}, isEncoder: ${mediaCodecInfo.isEncoder}, isSoftwareOnly: ${mediaCodecInfo.isSoftwareOnly}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            } else {
                Log.i(TAG, "mkvDecoders -> name: ${mediaCodecInfo.name}, isEncoder: ${mediaCodecInfo.isEncoder}, supportedTypes: ${mediaCodecInfo.supportedTypes.joinToString()}")
            }
            mediaCodecInfo.supportedTypes.forEach { mimeType: String ->
                if (mimeType.lowercase() == MediaFormat.MIMETYPE_VIDEO_HEVC){
                    val mediaCodecInfoCodecCapabilities: MediaCodecInfo.CodecCapabilities = mediaCodecInfo.getCapabilitiesForType(mimeType)
                    mediaCodecInfoCodecCapabilities.profileLevels.forEach { codecProfileLevel ->
                        if (codecProfileLevel.profile == MediaCodecInfo.CodecProfileLevel.HEVCProfileMain){
                            if (codecProfileLevel.level >= MediaCodecInfo.CodecProfileLevel.HEVCMainTierLevel62){
                                Log.i(TAG, "h265ToYuvPcm -> 支持 8k h265 name: ${mediaCodecInfo.name}")
                            } else {
                                Log.i(TAG, "h265ToYuvPcm -> 不支持 8k h265 name: ${mediaCodecInfo.name}")
                            }
                        }
                    }
                }
            }
        }

        val width = 672      /* 662 */
        val height = 1280

        val frameRate = 60
        val frameSize = width * height * 3 / 2

        val mediaFormat: MediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_BIT_RATE, 2_000_000) // 可根据分辨率调整
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val mediaCodec: MediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        val yuvInputStream: InputStream = context.contentResolver.openInputStream(yuvUri)!!
        val h264OutputStream: OutputStream = context.contentResolver.openOutputStream(h264Uri)!!
        val bytes = ByteArray(frameSize)

        mediaCodec.setCallback(object : MediaCodec.Callback() {
            override fun onError(
                codec: MediaCodec,
                e: MediaCodec.CodecException
            ) {
                Log.e(
                    TAG,
                    "onError name: ${codec.name}, thread: ${Thread.currentThread()}, error: ${e.message}",
                    e
                )
            }

            override fun onInputBufferAvailable(
                codec: MediaCodec,
                index: Int
            ) {
                Log.i(
                    TAG,
                    "onInputBufferAvailable -> name: ${mediaCodec.name}, index: $index, thread: ${Thread.currentThread()}"
                )
                val inputBuffer: ByteBuffer = codec.getInputBuffer(index) ?: return
                val size: Int = yuvInputStream.read(bytes, 0, frameSize)
                if (size == frameSize) {
                    inputBuffer.put(bytes, 0, size)
                    codec.queueInputBuffer(index, 0, size, System.nanoTime() / 1000, 0)
                } else {
                    codec.queueInputBuffer(
                        index,
                        0,
                        0,
                        System.nanoTime() / 1000,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM
                    )
                }
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                Log.i(
                    TAG,
                    "onOutputBufferAvailable -> name: ${codec.name}, index: $index, info: ${info.size}, thread: ${Thread.currentThread()}"
                )

                val outputBuffer: ByteBuffer = codec.getOutputBuffer(index) ?: return

                outputBuffer.get(bytes, 0, info.size)
                h264OutputStream.write(bytes, 0, info.size)

                codec.releaseOutputBuffer(index, false)

                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.i(TAG, "onOutputBufferAvailable -> == 编码结束...") // todo
                    yuvInputStream.close()
                    h264OutputStream.close()
                    if (continuation.isActive) {
                        Log.i(TAG, "pcmToAac -> 解码完成 resume before...")
                        continuation.resume(Unit)
                        Log.i(TAG, "pcmToAac -> 解码完成 resume after...")
                    }
                }
            }


            override fun onOutputFormatChanged(
                codec: MediaCodec,
                format: MediaFormat
            ) {
                Log.i(
                    TAG,
                    "onOutputFormatChanged -> name: ${codec.name}, format: $format"
                )
            }
        })
        Log.i(TAG, "pcmToAac -> before start...")
        mediaCodec.start()
        Log.i(TAG, "pcmToAac -> after start...")
    }

    /**
     * yuv420p 转 nv21
     * // hexdump -C clean.wav
     * // dd if=test.wav of=clean.wav bs=1 skip=2896
     * // ffplay -f s16le -ar 16000 -ch_layout mono test.pcm
     */
    fun yuv420pToNv21(yuv420p: ByteArray, nv12: ByteArray, width: Int, height: Int) {

        val frameSize: Int = width * height
        val qFrameSize: Int = frameSize / 4
        // Y 拷贝
        System.arraycopy(yuv420p, 0, nv12, 0, frameSize)

        val uStart: Int = frameSize
        val vStart: Int = frameSize + qFrameSize
        var uvIndex: Int = frameSize

        for (i in 0 until qFrameSize) {
            nv12[uvIndex++] = yuv420p[vStart + i] // V
            nv12[uvIndex++] = yuv420p[uStart + i] // U
        }
    }
} // https://source.android.google.cn/setup/start/build-numbers?hl=zsh-cn
// adb -s 192.168.31.232:36927 exec-out cat /sdcard/Download/output.yuv | ffplay -f rawvideo -pixel_format yuv420p -video_size 662x1280 -framerate 60 -
// adb -s 192.168.31.232:36927 exec-out cat /sdcard/Download/output.h264 | ffplay -f s16le -ar 44100 -ch_layout stereo -i -
// adb -s 192.168.31.232:36927 exec-out cat /sdcard/Download/output.h264 | ffplay -f rawvideo -pixel_format yuv420p -video_size 662x1280 -framerate 60 -
//  cmake -S . -B build
// ffplay -f rawvideo -pixel_format yuv420p -video_size 3840x2160 -framerate 60 output1.yuv
// ffplay -f rawvideo -pixel_format yuv420p -video_size 662x1280 -framerate 60 output1.yuv
// ffplay -map 0:0 -map 0:1 -vf "subtitles=video3.mkv:si=0" "video3.mkv"
// ffplay -x 1280 -y 720 -ss 00:50:00  video3.mkv
// nUs=15902600, display-height=4320, width=7680, color-range=2, max-input-size=1229468, frame-rate=30, height=4320,
