package io.github.imagecrop.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import io.github.imagecrop.R
import io.github.imagecrop.bean.CropArgs
import io.github.imagecrop.ui.theme.ImageCropTheme
import java.io.InputStream
import kotlin.math.roundToInt

private const val TAG: String = "CropScreen"

@Composable
private fun CropScreen1(cropArgs: CropArgs, modifier: Modifier = Modifier) {
    // val activity: Activity? = LocalActivity.current
    val context: Context = LocalContext.current
    // val configuration: Configuration = LocalConfiguration.current
    LaunchedEffect(key1 = Unit) {
        Log.i(TAG, "CropScreen -> cropArgs: $cropArgs")
    }
    var offset: Offset by remember {
        mutableStateOf(value = Offset.Zero)
    }
    Image(
        bitmap = try {
            // 图片等比压缩 -> 计算采样率
            context.contentResolver.openInputStream(cropArgs.input).use { inputStream: InputStream? ->
                BitmapFactory.decodeStream(inputStream).asImageBitmap()
            }
        } catch (e: Exception) {
            Log.e(TAG, "CropScreen -> error: ${e.message}", e)
            val vectorDrawable: Drawable? = ResourcesCompat.getDrawable(context.resources, R.drawable.error_placeholder, null)
            val imageBitmap: ImageBitmap = vectorDrawable?.toBitmap(width = vectorDrawable.intrinsicWidth, height = vectorDrawable.intrinsicHeight)?.asImageBitmap() ?: 0xFFBB11AA.toInt().toDrawable().toBitmap(width = 100, height = 100).asImageBitmap()
            imageBitmap
        },
        contentDescription = "裁剪图片结果",
        modifier = Modifier.fillMaxSize()
                .offset{
                    IntOffset(x = offset.x.roundToInt(), y = offset.y.roundToInt())
                }
                .pointerInput(key1 = Unit){
                    detectDragGestures { change: PointerInputChange, dragAmount: Offset ->
                        change.consume()
                        offset += dragAmount
                    }
                }
                .drawWithCache {
                    val strokeWidth: Float = 5.dp.toPx()
                    val dashLengthPx: Float = 5.dp.toPx()
                    val gapLengthPx: Float = 4.dp.toPx()
                    onDrawBehind {
                        val path: Path = Path().apply {
                            addRect(
                                Rect(offset = Offset.Zero, size = size)
                            )
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFE1E1E1),
                            style = Stroke(
                                width = strokeWidth,
                                pathEffect = PathEffect.dashPathEffect(
                                    intervals = floatArrayOf(dashLengthPx, gapLengthPx),
                                    phase = 0F
                                )
                            )
                        )
                    }
                }
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    ImageCropTheme {
        CropScreen1(cropArgs = CropArgs(input = Uri.EMPTY))
    }
}
/*
private fun decodeImageSafely(context: Context, uri: Uri): ImageBitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, this)

                // 计算合适的采样率
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                inJustDecodeBounds = false

                // 重新打开流
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, this)?.asImageBitmap()
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error decoding image", e)
        null
    }
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

/*
fun decodeSampledBitmap(resources: Resources, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        BitmapFactory.decodeResource(resources, resId, this)

        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        inJustDecodeBounds = false
    }

    return BitmapFactory.decodeResource(resources, resId, options)
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height, width) = options.run { outHeight to outWidth }
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        while (halfHeight / inSampleSize >= reqHeight &&
               halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
 */

/*
fun scaleBitmap(original: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
    val aspectRatio = original.width.toFloat() / original.height.toFloat()
    val scaledWidth: Int
    val scaledHeight: Int

    if (targetWidth / aspectRatio <= targetHeight) {
        scaledWidth = targetWidth
        scaledHeight = (targetWidth / aspectRatio).toInt()
    } else {
        scaledWidth = (targetHeight * aspectRatio).toInt()
        scaledHeight = targetHeight
    }

    return Bitmap.createScaledBitmap(original, scaledWidth, scaledHeight, true)
}
 */

/*
@Composable
internal fun CropScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState,
    helloViewModel: HelloViewModel = hiltViewModel<HelloViewModel>(),
    uri: Uri = "content://media/external/images/media/1000005861".toUri(),
) {
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    // Box(
    //     modifier = modifier.fillMaxSize()
    // ) {
    //     Image(
    //         bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
    //             BitmapFactory.decodeStream(inputStream)
    //         }!!.asImageBitmap(),
    //         contentDescription = "图片裁剪",
    //         modifier = Modifier.fillMaxSize().drawWithContent{
    //             drawContent()
    //             drawRect(
    //                 color = Color.Cyan,
    //                 topLeft = Offset(0F, 0F),
    //                 size = Size(100F, 100F),
    //                 style = Stroke(width = 2.dp.toPx())
    //             )
    //         }
    //     )
    // }

    // var offset: Offset by remember {
    //     mutableStateOf(value = Offset.Zero)
    // }
    //
    // Column(
    //     modifier = Modifier.fillMaxSize()
    //         .offset{
    //             IntOffset(x = offset.x.roundToInt(), y = offset.y.roundToInt())
    //         }
    //         .pointerInput(key1 = Unit){
    //             detectDragGestures { change: PointerInputChange, dragAmount: Offset ->
    //                 change.consume()
    //                 offset += dragAmount
    //             }
    //         }
    //         .drawWithCache {
    //             val strokeWidth: Float = 5.dp.toPx()
    //             val dashLengthPx: Float = 5.dp.toPx()
    //             val gapLengthPx: Float = 4.dp.toPx()
    //             onDrawBehind {
    //                 val path: Path = Path().apply {
    //                     addRect(
    //                         Rect(offset = Offset.Zero, size = size)
    //                     )
    //                 }
    //                 drawPath(
    //                     path = path,
    //                     color = Color(0xFFE1E1E1),
    //                     style = Stroke(
    //                         width = strokeWidth,
    //                         pathEffect = PathEffect.dashPathEffect(
    //                             intervals = floatArrayOf(dashLengthPx, gapLengthPx),
    //                             phase = 0F
    //                         )
    //                     )
    //                 )
    //             }
    //         }
    // ){
    //     Text(
    //         text = "设置State",
    //         Modifier
    //             .padding(top = 10.dp)
    //             .background(color = Color.Black, shape = RoundedCornerShape10)
    //             .padding(all = 5.dp)
    //             .clickable {
    //                 helloViewModel.setSaveData(value = "zshh")
    //             },
    //         color = Color.White
    //     )
    //     Text(
    //         text = "获取State",
    //         Modifier
    //             .padding(top = 10.dp)
    //             .background(color = Color.Black, shape = RoundedCornerShape10)
    //             .padding(all = 5.dp)
    //             .clickable {
    //                 coroutineScope.launch {
    //                     snackBarHostState.showSnackbar("data: ${helloViewModel.getSaveData()}")
    //                 }
    //             },
    //         color = Color.White
    //     )
    // }
 */