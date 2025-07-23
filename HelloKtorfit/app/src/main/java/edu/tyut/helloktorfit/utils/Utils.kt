package edu.tyut.helloktorfit.utils

import android.content.ContentValues
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.contentValuesOf
import androidx.core.graphics.scale
import kotlinx.io.Buffer
import java.io.BufferedInputStream
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

            Log.i(TAG, "decodeSampledBitmap step1 -> imageType: ${outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${outWidth}, height: ${outHeight}")
            bufferedInputStream.reset()
            Log.i(TAG, "decodeSampledBitmap step2 -> imageType: ${outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${outWidth}, height: ${outHeight}")
            BitmapFactory.decodeStream(bufferedInputStream, null,  this)!!.apply {
                Log.i(TAG, "decodeSampledBitmap step3 -> imageType: ${outMimeType}, reqWidth: $reqWidth, reqHeight: $reqHeight, width: ${outWidth}, height: ${outHeight}")
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