package io.github.okhttplearn.utils

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaPlayer
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.gzip
import okio.sink
import okio.source
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.GZIPOutputStream
import kotlin.jvm.Throws

private const val TAG: String = "Utils"

internal object Utils {

    init {
        System.loadLibrary("okhttpLearn1")
        System.loadLibrary("okhttpLearn2")
    }

    internal fun compressFileToGzip1(sourceFile: File, targetFile: File) {
        targetFile.outputStream().buffered().use { fileOutputStream: BufferedOutputStream ->
            GZIPOutputStream(fileOutputStream).buffered()
                .use { gzipOutputStream: BufferedOutputStream ->
                    sourceFile.inputStream().buffered()
                        .use { bufferedInputStream: BufferedInputStream ->
                            bufferedInputStream.copyTo(out = gzipOutputStream)
                        }
                }
        }
    }

    /**
     * ```shell
     * PS C:\Users\29051> (Get-FileHash -Path C:\Users\29051\Downloads\dest.gz -Algorithm MD5).Hash.ToLower()
     * 98cbb6b41e3a4aeec9e35dfec9351673
     * ```
     */
    internal fun compressFileToGzip2(sourceFile: File, targetFile: File) {
        targetFile.sink().gzip().buffer().use { bufferedSink: BufferedSink ->
            sourceFile.source().buffer().use { bufferedSource: BufferedSource ->
                bufferedSource.readAll(sink = bufferedSink)
            }
        }
    }

    internal fun getFileMd5(file: File): String {
        val digest: MessageDigest = MessageDigest.getInstance("MD5")
        file.inputStream().buffered().use { inputStream: InputStream ->
            val bytes = ByteArray(1024 * 8)
            var length: Int
            while (inputStream.read(bytes).also { length = it } > 0) {
                digest.update(bytes, 0, length)
            }
            val md5Bytes: ByteArray = digest.digest()
            return md5Bytes.joinToString(separator = "") { "%02x".format(it) }
        }
    }

    @Throws(exceptionClasses = [RuntimeException::class])
    private external fun nativeEncoder(): Long
    @Throws(exceptionClasses = [RuntimeException::class])
    internal fun encoder() : Long {
        return this.nativeEncoder()
    }

    private external fun nativeReleaseEncoder(ptr: Long)
    internal fun releaseEncoder(ptr: Long) {
        this.nativeReleaseEncoder(ptr = ptr)
    }
}