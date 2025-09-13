package io.github.imagecrop.ui.screen

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import io.github.imagecrop.R
import io.github.imagecrop.bean.CropArgs
import io.github.imagecrop.contract.CropContract
import io.github.imagecrop.ui.theme.ImageCropTheme
import java.io.File
import java.io.InputStream

private const val TAG: String = "Greeting"

@Composable
internal fun Greeting(modifier: Modifier = Modifier) {
    val context: Context = LocalContext.current
    var uri: Uri by rememberSaveable {
        mutableStateOf(value = Uri.EMPTY)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = CropContract()
    ) { imageUri: Uri ->
        Log.i(TAG, "Greeting uri: $uri")
        val output = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "result.jpg").toUri()
        uri = output
    }
    val imageLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // 权限问题会空指针
        val output = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "result.jpg").toUri()
        Log.i(TAG, "Greeting -> output: $output")
        launcher.launch(input = CropArgs(input = uri!!, output = output))
    }
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
    Column(modifier = modifier.fillMaxSize()){
        Text(
            text = "Select Image",
            modifier = Modifier.clickable {
                val request = PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build()
                imageLauncher.launch(input = request, options = ActivityOptionsCompat.makeBasic())
            }
        )
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = try {
                // 图片等比压缩 -> 计算采样率
                if (uri != Uri.EMPTY){
                    context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                        BitmapFactory.decodeStream(inputStream).asImageBitmap()
                    }!!
                } else {
                    defaultImageBitmap
                }
            } catch (e: Exception) {
                Log.e(TAG, "CropScreen -> error: ${e.message}", e)
                defaultImageBitmap
            },
            contentDescription = "裁剪图片结果",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    ImageCropTheme {
        Greeting()
    }
}