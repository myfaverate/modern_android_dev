package io.github.imagecrop.ui.screen

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import io.github.imagecrop.R
import io.github.imagecrop.bean.CropArgs
import io.github.imagecrop.ui.activity.CropActivity
import io.github.imagecrop.ui.theme.ImageCropTheme
import io.github.imagecrop.utils.Utils
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

private const val TAG: String = "CropScreen"
private const val CROP_PADDING: Float = 10F

private enum class Corner {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, MOVE, NONE
}

@Composable
internal fun CropScreen(cropArgs: CropArgs, modifier: Modifier = Modifier) {

    val activity: Activity? = LocalActivity.current
    val density: Density = LocalDensity.current
    val context: Context = LocalContext.current
    val configuration: Configuration = LocalConfiguration.current

    /**
     * ImageView 的大小
     */
    var imageSize: IntSize by remember {
        mutableStateOf(
            value = IntSize.Zero
        )
    }

    /**
     * 图片真实区域
     */
    var imageRect: Rect by remember {
        mutableStateOf(value = Rect.Zero)
    }

    /**
     * 裁剪框区域
     */
    var rect: Rect by remember {
        mutableStateOf(value = Rect.Zero)
    }

    /**
     * 拖动裁剪框四角还是拖动裁剪框本身
     */
    var activeCorner: Corner by remember {
        mutableStateOf(value = Corner.NONE)
    }

    /**
     * 裁剪框 圆角大小
     */
    val radius: Float = with(receiver = density) { 10.dp.toPx() }

    /**
     * 默认占位图
     */
    val defaultImageBitmap: ImageBitmap by remember {
        val vectorDrawable: Drawable? = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.error_placeholder,
            null
        )
        val defaultImageBitmap: ImageBitmap = vectorDrawable?.toBitmap(
            width = vectorDrawable.intrinsicWidth,
            height = vectorDrawable.intrinsicHeight
        )?.asImageBitmap() ?: 0xFFBB11AA.toInt().toDrawable().toBitmap(width = 100, height = 100).asImageBitmap()
        mutableStateOf(value = defaultImageBitmap)
    }

    /**
     * ffmpeg -y -i image1.jpg -vf scale=16200:16200 output.png 最大分辨率图片, 可惜Android不支持, 获取OpenGL纹理可以解决
     * 加载的真实图片
     */
    val imageBitmap: ImageBitmap by produceState<ImageBitmap>(
        initialValue = defaultImageBitmap
    ) {
        try {
            val bitmap: Bitmap? = context.contentResolver.openInputStream(cropArgs.input)
                ?.use { inputStream: InputStream ->
                Log.i(TAG, "CropScreen -> before: ${imageSize.width}, height: ${imageSize.height}")
                Utils.decodeSampledBitmapFromResource(
                    inputStream,
                    imageSize.width,
                    imageSize.height
                )
            }
            value = bitmap?.asImageBitmap() ?: defaultImageBitmap
        } catch (e: Exception) {
            Log.e(TAG, "Image load failed: ${e.message}", e)
            value = defaultImageBitmap
        }
    }

    LaunchedEffect(key1 = Unit) {
        Log.i(TAG, "CropScreen -> imageBitmapWidth: ${imageBitmap.width}, imageBitmapHeight: ${imageBitmap.height}, imageSize: $imageSize")
        // 计算图片裁剪框的大小
        val scale: Float = min(imageSize.width / imageBitmap.width.toFloat(), imageSize.height / imageBitmap.height.toFloat())
        Log.i(
            TAG,
            "CropScreen -> scale: $scale imageBitmapWidth: ${imageBitmap.width}, imageBitmapHeight: ${imageBitmap.height}}"
        )
        val imageWidth: Float = imageBitmap.width * scale
        val imageHeight: Float = imageBitmap.height * scale
        val left: Float = (imageSize.width - imageWidth) / 2F // 居中
        val top: Float = (imageSize.height - imageHeight) / 2F
        val right: Float = left + imageWidth
        val bottom: Float = top + imageHeight
        imageRect = Rect(Offset(left, top), Offset(right, bottom))
        // val cropPadding: Float = with(density) { CROP_PADDING.dp.toPx() }
        // rect = rect.copy(
        //     left + cropPadding,
        //     top + cropPadding,
        //     right - cropPadding,
        //     bottom - cropPadding
        // )
        if (cropArgs.aspectRatio[0] != 0 && cropArgs.aspectRatio[1] != 0){
            val ratio: Float = cropArgs.aspectRatio[0] / cropArgs.aspectRatio[1].toFloat()

            // 尝试用 imageRect 的宽来算裁剪框高度
            var cropWidth: Float = imageWidth
            var cropHeight: Float = cropWidth / ratio

            // 如果高度超过了 imageRect，则用高度来算宽
            if (cropHeight > imageHeight) {
                cropHeight = imageHeight
                cropWidth = cropHeight * ratio
            }

            // 居中
            val left: Float = imageRect.left + (imageWidth - cropWidth) / 2f
            val top: Float = imageRect.top + (imageHeight - cropHeight) / 2f
            val right: Float = left + cropWidth
            val bottom: Float = top + cropHeight

            rect = Rect(left, top, right, bottom)
        } else {
            rect = imageRect
        }
    }
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "裁剪图片结果",
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0x69000000))
                .onSizeChanged { size ->
                    imageSize = size
                }
        )
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(key1 = Unit) {
                    detectDragGestures(
                        onDragStart = { offset: Offset ->
                            activeCorner = when {
                                // 触控点到圆点的距离小于直径
                                (offset - Offset(
                                    rect.left,
                                    rect.top
                                )).getDistance() < radius * 2 -> Corner.TOP_LEFT

                                (offset - Offset(
                                    rect.right,
                                    rect.top
                                )).getDistance() < radius * 2 -> Corner.TOP_RIGHT

                                (offset - Offset(
                                    rect.left,
                                    rect.bottom
                                )).getDistance() < radius * 2 -> Corner.BOTTOM_LEFT

                                (offset - Offset(
                                    rect.right,
                                    rect.bottom
                                )).getDistance() < radius * 2 -> Corner.BOTTOM_RIGHT

                                rect.contains(offset) -> Corner.MOVE
                                else -> Corner.NONE
                            }
                        },
                        onDragEnd = { activeCorner = Corner.NONE },
                        onDragCancel = { activeCorner = Corner.NONE },
                        onDrag = { pointerInputChange: PointerInputChange, offset: Offset ->
                            pointerInputChange.consume()
                            val minCropWidth = 100F
                            val cropLeft: Float = imageRect.left
                            val cropTop: Float = imageRect.top
                            val cropRight: Float = imageRect.right
                            val cropBottom: Float = imageRect.bottom
                            val ratio: Float =  cropArgs.aspectRatio[0] / cropArgs.aspectRatio[1].toFloat()
                            rect = when (activeCorner) {
                                Corner.TOP_LEFT -> {
                                    // 优化
                                    var left: Float =
                                        (rect.left + offset.x).coerceIn(
                                            cropLeft,
                                            rect.right - minCropWidth
                                        )
                                    var top: Float =
                                        (rect.top + offset.y).coerceIn(
                                            cropTop,
                                            rect.bottom - minCropWidth
                                        )
                                    // 固定比例
                                    if (cropArgs.aspectRatio[0] != 0 && cropArgs.aspectRatio[1] != 0){
                                        val width: Float = rect.right - left
                                        var height: Float = width / ratio
                                        top = rect.bottom - height
                                        if (top < cropTop) {
                                            top = cropTop
                                            height = rect.bottom - top
                                            left = rect.right - height * ratio
                                        }
                                    }
                                    rect.copy(
                                        left = left,
                                        top = top
                                    )
                                }

                                Corner.TOP_RIGHT -> {
                                    // 优化
                                    var right: Float =
                                        (rect.right + offset.x).coerceIn(
                                            rect.left + minCropWidth,
                                            cropRight
                                        )
                                    var top: Float =
                                        (rect.top + offset.y).coerceIn(
                                            cropTop,
                                            rect.bottom - minCropWidth
                                        )
                                    // 固定比例
                                    if (cropArgs.aspectRatio[0] != 0 && cropArgs.aspectRatio[1] != 0){
                                        val width = right - rect.left
                                        var height = width / ratio
                                        top = rect.bottom - height
                                        if (top < cropTop) {
                                            top = cropTop
                                            height = rect.bottom - top
                                            right = rect.left + height * ratio
                                        }
                                    }
                                    rect.copy(
                                        right = right,
                                        top = top
                                    )
                                }

                                Corner.BOTTOM_LEFT -> {
                                    var left: Float =
                                        (rect.left + offset.x).coerceIn(
                                            cropLeft,
                                            rect.right - minCropWidth
                                        )
                                    var bottom: Float =
                                        (rect.bottom + offset.y).coerceIn(
                                            rect.top + minCropWidth,
                                            cropBottom
                                        )
                                    // 固定比例
                                    if (cropArgs.aspectRatio[0] != 0 && cropArgs.aspectRatio[1] != 0){
                                        val width = rect.right - left
                                        var height = width / ratio
                                        bottom = rect.top + height
                                        if (bottom > cropBottom) {
                                            bottom = cropBottom
                                            height = bottom - rect.top
                                            left = rect.right - height * ratio
                                        }
                                    }
                                    rect.copy(
                                        left = left,
                                        bottom = bottom
                                    )
                                }

                                Corner.BOTTOM_RIGHT -> {
                                    var right: Float =
                                        (rect.right + offset.x).coerceIn(
                                            rect.left + minCropWidth,
                                            cropRight
                                        )
                                    var bottom: Float =
                                        (rect.bottom + offset.y).coerceIn(
                                            rect.top + minCropWidth,
                                            cropBottom
                                        )
                                    // 固定比例
                                    if (cropArgs.aspectRatio[0] != 0 && cropArgs.aspectRatio[1] != 0){
                                        val width = right - rect.left
                                        var height = width / ratio
                                        bottom = rect.top + height
                                        if (bottom > cropBottom) {
                                            bottom = cropBottom
                                            height = bottom - rect.top
                                            right = rect.left + height * ratio
                                        }
                                    }
                                    rect.copy(
                                        right = right,
                                        bottom = bottom
                                    )
                                }

                                Corner.MOVE -> {
                                    val moved: Rect = rect.translate(offset)
                                    val dx: Float = when {
                                        moved.left < cropLeft -> cropLeft - rect.left
                                        moved.right > cropRight -> cropRight - rect.right
                                        else -> offset.x
                                    }
                                    val dy: Float = when {
                                        moved.top < cropTop -> cropTop - rect.top
                                        moved.bottom > cropBottom -> cropBottom - rect.bottom
                                        else -> offset.y
                                    }
                                    rect.translate(offset = offset.copy(dx, dy))
                                }

                                Corner.NONE -> rect
                            }
                            Log.i(
                                TAG,
                                "CropScreen -> rect: $rect, width: ${rect.width}, height: ${rect.height}"
                            )
                        },
                    )
                }
        ) {
            val cropBorderWidth: Float = with(density) { 1.5F.dp.toPx() }
            val lineWidth: Float = with(density) { 0.5F.dp.toPx() }
            // 裁剪框四个圆角
            drawCircle(Color.White, center = Offset(x = rect.left, rect.top), radius = radius)
            drawCircle(Color.White, center = Offset(x = rect.right, rect.top), radius = radius)
            drawCircle(Color.White, center = Offset(x = rect.left, rect.bottom), radius = radius)
            drawCircle(Color.White, center = Offset(x = rect.right, rect.bottom), radius = radius)
            // 裁剪框
            drawRect(
                color = Color.White,
                topLeft = Offset(x = rect.left, y = rect.top),
                size = Size(width = rect.width, height = rect.height),
                style = Stroke(width = cropBorderWidth)
            )
            // 九宫格
            // 两横、两束
            for (i in 1 .. 2){
                drawLine(color = Color.White, start = Offset(x = rect.left, rect.top + i * rect.height / 3F), end = Offset(x = rect.right, y = rect.top + i * rect.height / 3F), strokeWidth = lineWidth)
            }
            for (i in 1 .. 2){
                drawLine(color = Color.White, start = Offset(x = rect.left + i * rect.width / 3, rect.top), end = Offset(x = rect.left + i * rect.width / 3, y = rect.bottom), strokeWidth = lineWidth)
            }
        }
        Icon(
            imageVector = Icons.Filled.Clear,
            contentDescription = "退出",
            modifier = Modifier
                .align(alignment = Alignment.BottomStart)
                .padding(19.dp)
                .clickable {
                    Log.i(TAG, "CropScreen -> 退出")
                    activity?.setResult(Activity.RESULT_CANCELED)
                    activity?.finish()
                },
            tint = Color.White
        )
        Icon(
            imageVector = Icons.Filled.Done,
            contentDescription = "确定",
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .padding(19.dp)
                .clickable {
                    Log.i(TAG, "CropScreen -> 确定")

                    val options: BitmapFactory.Options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(cropArgs.input).use {
                        BitmapFactory.decodeStream(it, null, options)
                    }

                    val inputWidth: Int = options.outWidth
                    val inputHeight: Int = options.outHeight

                    // 计算裁剪区域（从屏幕映射到原图）
                    val scaleX: Float = inputWidth / imageRect.width
                    val scaleY: Float = inputHeight / imageRect.height

                    val cropLeft: Int = ((rect.left - imageRect.left) * scaleX).toInt()
                    val cropTop: Int = ((rect.top - imageRect.top) * scaleY).toInt()
                    val cropRight: Int = ((rect.right - imageRect.left) * scaleX).toInt()
                    val cropBottom: Int = ((rect.bottom - imageRect.top) * scaleY).toInt()
                    val cropRect = android.graphics.Rect(cropLeft, cropTop, cropRight, cropBottom)

                    context.contentResolver.openInputStream(cropArgs.input)?.use { inputStream: InputStream ->
                        context.contentResolver.openOutputStream(cropArgs.output)?.use { outputStream: OutputStream ->
                            val decoder: BitmapRegionDecoder? =
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    BitmapRegionDecoder.newInstance(inputStream)
                                } else {
                                    @Suppress("DEPRECATION")
                                    BitmapRegionDecoder.newInstance(inputStream, false)
                                }
                            val options: BitmapFactory.Options = BitmapFactory.Options().apply {
                                inSampleSize = Utils.calculateInSampleSize(imageWidth = cropRect.width(), imageHeight = cropRect.height(), cropArgs.maxResultSize[0], cropArgs.maxResultSize[1])
                            }
                            val bitmap: Bitmap? = decoder?.decodeRegion(cropRect, options)
                            val isSuccess: Boolean? = bitmap?.compress(
                                Bitmap.CompressFormat.JPEG,
                                100,
                                outputStream
                            )
                            Log.i(TAG, "CropScreen -> 是否裁剪成功: $isSuccess")
                            if (isSuccess == true){
                                activity?.setResult(Activity.RESULT_OK)
                            } else {
                                activity?.setResult(CropActivity.CROP_FAILURE)
                            }
                            activity?.finish()
                        }
                    }
                },
            tint = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    ImageCropTheme {
        CropScreen(cropArgs = CropArgs(input = Uri.EMPTY, output = Uri.EMPTY))
    }
}