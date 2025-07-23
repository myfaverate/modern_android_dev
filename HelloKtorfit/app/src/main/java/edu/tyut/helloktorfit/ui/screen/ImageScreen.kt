package edu.tyut.helloktorfit.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import edu.tyut.helloktorfit.utils.Utils
import edu.tyut.helloktorfit.viewmodel.HelloViewModel
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.toMap
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.BufferedInputStream
import java.io.InputStream

private const val TAG: String = "ImageScreen"

@Composable
internal fun ImageScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
    helloViewModel: HelloViewModel = hiltViewModel<HelloViewModel>()
) {
    val context: Context = LocalContext.current
    // val coroutineScope: CoroutineScope = rememberCoroutineScope()
    var imageSize: IntSize by remember {
        mutableStateOf(value = IntSize.Zero)
    }
    val defaultBitmap: ImageBitmap =
        0xFFBB11AA.toInt().toDrawable().toBitmap(width = 100, height = 100).asImageBitmap()
    val imageBitmap: ImageBitmap by produceState<ImageBitmap>(
        initialValue = defaultBitmap
    ) {
        try {
            val bitmap: Bitmap = helloViewModel.getImage(imageName = "bigImage.jpg")
                .execute { httpResponse: HttpResponse ->
                    httpResponse.headers.toString()
                    Log.i(
                        TAG,
                        "ImageScreen -> headers: ${httpResponse.headers.toMap()}, imageSize: $imageSize"
                    )
                    httpResponse.bodyAsChannel().toInputStream().use { inputStream: InputStream ->
                        Utils.decodeSampledBitmapFromResource(
                            inputStream,
                            imageSize.width / 2,
                            imageSize.height / 2
                        )
                    }
                }
            value = bitmap.asImageBitmap()
        } catch (e: Exception) {
            Log.e(TAG, "Image load failed: ${e.message}", e)
            value = defaultBitmap
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { /* 尺寸变化 */ size: IntSize ->
                    imageSize = size
                }
                .clickable {
                    val originBitmap: Bitmap = imageBitmap.asAndroidBitmap()
                    Utils.saveImage(context = context, bitmap = originBitmap)?.let { uri: Uri ->
                        context.contentResolver.openInputStream(uri)
                            ?.use { inputStream: InputStream ->
                                Log.i(TAG, "ImageScreen -> imageSize: $imageSize ")
                                val bitmap: Bitmap = Utils.decodeSampledBitmapFromResource(
                                    inputStream,
                                    imageSize.width / 2,
                                    imageSize.height / 2
                                )
                                Utils.saveImage(context, bitmap)
                            }
                    }
                },
            contentDescription = "图片",
            bitmap = imageBitmap,
        )
    }
}