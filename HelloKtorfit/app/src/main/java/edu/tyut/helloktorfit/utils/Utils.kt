package edu.tyut.helloktorfit.utils

import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.contentValuesOf
import androidx.core.graphics.scale
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

private const val TAG: String = "Utils"

internal object Utils {
    

    internal fun scaleBitmap(original: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val aspectRatio: Float = original.width.toFloat() / original.height.toFloat()
        val scaledWidth: Int
        val scaledHeight: Int
        if (targetWidth / aspectRatio <= targetHeight) {
            scaledWidth = targetWidth
            scaledHeight = (targetWidth / aspectRatio).toInt()
        } else {
            scaledWidth = (targetHeight * aspectRatio).toInt()
            scaledHeight = targetHeight
        }
        return original.scale(scaledWidth, scaledHeight)
    }

    internal fun decodeSampledBitmap(inputStream: InputStream, reqWidth: Int, reqHeight: Int): Bitmap {
        val bufferedInputStream: BufferedInputStream = inputStream.buffered().apply { mark(Int.MAX_VALUE) }
        val options: BitmapFactory.Options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(bufferedInputStream, null,  this)
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            Log.i(TAG, "decodeSampledBitmap -> inSampleSize: $inSampleSize")
            inJustDecodeBounds = false
        }
        Log.i(TAG, "decodeSampledBitmap -> imageType: ${options.outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${options.outWidth}, height: ${options.outHeight}")
        bufferedInputStream.reset()
        return BitmapFactory.decodeStream(bufferedInputStream, null,  options)!!
    }

    internal fun decodeSampledBitmapFromResource(
        inputStream: InputStream,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        val bufferedInputStream: BufferedInputStream = inputStream.buffered().apply { mark(Int.MAX_VALUE) }
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                inPreferredConfig = Bitmap.Config.HARDWARE
            }
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(bufferedInputStream, null,  this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            Log.i(TAG, "decodeSampledBitmap step1 -> imageType: ${outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${outWidth}, height: $outHeight")
            bufferedInputStream.reset()
            Log.i(TAG, "decodeSampledBitmap step2 -> imageType: ${outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${outWidth}, height: $outHeight")
            BitmapFactory.decodeStream(bufferedInputStream, null,  this)!!.apply {
                Log.i(TAG, "decodeSampledBitmap step3 -> imageType: ${outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${outWidth}, height: $outHeight")
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    internal fun saveImage(context: Context, bitmap: Bitmap): Uri? {
        val contentValues: ContentValues = contentValuesOf(
            MediaStore.Images.Media.DISPLAY_NAME to "image_${System.currentTimeMillis()}.jpg",
            MediaStore.Images.Media.MIME_TYPE to "image/jpeg",
            MediaStore.Images.Media.RELATIVE_PATH to Environment.DIRECTORY_PICTURES
        )
        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.also { uri: Uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
                val isSuccess: Boolean = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                Log.i(TAG, "saveImage -> isSuccess: $isSuccess, uri: $uri")
            }
        }
        return uri
    }
    internal fun getProcessName(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.os.Process.myProcessName()
        } else {
            val pid: Int = Process.myPid()
            val activityManager: ActivityManager = context.getSystemService<ActivityManager>(
                ActivityManager::class.java)
            val processName: String = activityManager.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName ?: "unknown"
            return processName
        }
    }
    internal fun sha256sum(filePath: String = ""): String {
        Log.i(TAG, "sha256sum -> properties: ${System.getProperties()}")
        Log.i(TAG, "sha256sum -> getenv: ${System.getenv()}")
        val process: java.lang.Process = Runtime.getRuntime().exec(
            arrayOf<String>(
                "sha256sum", filePath
            )
        )
        val result: String = process.errorStream.bufferedReader().use { it.readText() }
        Log.i(TAG, "sha256sum -> error: $result")
        if (result.isNotEmpty()){
            return result
        }
        return process.inputStream.bufferedReader().use { bufferedInputStream ->
            val result: String = bufferedInputStream.readText()
            Log.i(TAG, "sha256sum -> result: $result, path: ${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}")
            result
        }
    }
    /**
     * 获取 aac adts 非CRC 裸流时长
     * 返回单位为s
     */
    @JvmStatic
    fun calculateAacAdtsDuration(aacPath: String): Double {
        val aacFile = File(aacPath)
        var frameCount = 0
        var sampleRate = 44100 // fallback
        val buffer = ByteArray(7)
        aacFile.inputStream().use { inputStream ->
            while (inputStream.read(buffer, 0, 7) == 7) {
                if (buffer[0] != 0xFF.toByte() || (buffer[1].toInt() and 0xF0) != 0xF0) {
                    break // 非 ADTS 帧
                }
                // 采样率索引
                val freqIndex = (buffer[2].toInt() and 0x3C) shr 2
                sampleRate = samplingFrequencyFromIndex(freqIndex)
                // 帧长度（13 位）
                val frameLen =
                    ((buffer[3].toInt() and 0x03) shl 11) or
                            ((buffer[4].toInt() and 0xFF) shl 3) or
                            ((buffer[5].toInt() and 0xE0) shr 5)

                // 跳过剩余数据（帧总长 - 头部）
                inputStream.skip((frameLen - 7).toLong())
                frameCount++
            }
        }
        return frameCount * 1024.0 / sampleRate // 秒
    }
    // 编译器inline优化
    private fun samplingFrequencyFromIndex(index: Int): Int = when (index) {
        0 -> 96000
        1 -> 88200
        2 -> 64000
        3 -> 48000
        4 -> 44100
        5 -> 32000
        6 -> 24000
        7 -> 22050
        8 -> 16000
        9 -> 12000
        10 -> 11025
        11 -> 8000
        12 -> 7350
        else -> 44100
    }

    @JvmStatic
    fun getVideoDuration(videoPath: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoPath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "获取视频时长失败", e)
            0L
        } finally {
            retriever.release()
        }
    }

}
/*
internal fun decodeSampledBitmap(resources: Resources, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
        val options: BitmapFactory.Options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, resId, this)
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
        }
        return BitmapFactory.decodeResource(resources, resId, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
 */