package edu.tyut.helloktorfit.ui.screen

import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import edu.tyut.helloktorfit.utils.Utils
import edu.tyut.helloktorfit.viewmodel.HelloViewModel
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.toMap
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.InputStream

private const val TAG: String = "GifScreen"

@Composable
internal fun GifScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
    helloViewModel: HelloViewModel = hiltViewModel<HelloViewModel>()
) {
    var imageSize: IntSize by remember {
        mutableStateOf(value = IntSize.Zero)
    }
    val defaultImageBitmap: ImageBitmap = 0xFFAADDEE.toInt().toDrawable().toBitmap(width = 100, height = 100).asImageBitmap()
    val imageBitmap: ImageBitmap by produceState<ImageBitmap>(
        initialValue = defaultImageBitmap
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
                        Utils.decodeSampledBitmapFromResource(inputStream = inputStream, reqWidth = imageSize.width, reqHeight = imageSize.height)
                        // BitmapFactory.decodeStream(inputStream)
                    }
                }
            value = bitmap.asImageBitmap()
        } catch (e: Exception) {
            Log.e(TAG, "Image load failed: ${e.message}", e)
            value = defaultImageBitmap
        }
    }
    Image(
        bitmap = imageBitmap,
        contentDescription = "图片加载",
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Gray)
            .onSizeChanged { /* 尺寸变化 */ size: IntSize ->
                imageSize = size
            }
    )
}