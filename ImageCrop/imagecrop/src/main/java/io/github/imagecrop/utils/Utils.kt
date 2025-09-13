package io.github.imagecrop.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import java.io.BufferedInputStream
import java.io.InputStream

private const val TAG: String = "Utils"

internal object Utils {
    @JvmStatic
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
            Log.i(TAG, "decodeSampledBitmapFromResource -> inSampleSize: $inSampleSize")

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            Log.i(TAG, "decodeSampledBitmap step1 -> imageType: ${outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${outWidth}, height: $outHeight")
            bufferedInputStream.reset()
            val startTime: Long = System.currentTimeMillis()
            Log.i(TAG, "decodeSampledBitmap step2 -> imageType: ${outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${outWidth}, height: $outHeight")
            BitmapFactory.decodeStream(bufferedInputStream, null,  this)!!.apply {
                Log.i(TAG, "decodeSampledBitmap step3 -> imageType: ${outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${outWidth}, height: $outHeight, duration: ${(System.currentTimeMillis() - startTime) / 1000}s")
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
    @JvmStatic
    internal fun calculateInSampleSize(imageWidth: Int, imageHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = imageWidth to imageHeight
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
}
/*
val decoder = BitmapRegionDecoder.newInstance(inputStream, false)
val rect = Rect(0, 0, 2000, 2000) // 想要的区域
val options = BitmapFactory.Options().apply {
    inSampleSize = 4
}
val bitmap = decoder.decodeRegion(rect, options)
 */