package edu.tyut.helloktorfit.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import edu.tyut.helloktorfit.contract.CropContract
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape10
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

private const val TAG: String = "CropScreen"

@Composable
internal fun CropScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState,
) {
    val context: Context = LocalContext.current
    var imageUri: Uri by remember {
        mutableStateOf(value = Uri.EMPTY)
    }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val permissions: Array<String> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val permissionsLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { map: Map<String, @JvmSuppressWildcards Boolean> ->
        coroutineScope.launch {
            snackBarHostState.showSnackbar("获取权限${if (map.values.all { it }) "成功" else "失败"}")
        }
    }
    val cropLauncher = rememberLauncherForActivityResult(
        contract = CropContract(context = context)
    ) { uri: Uri? ->
        Log.i(TAG, "cropLauncher -> uri: $uri")
        imageUri = uri!!
    }
    val launcher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        Log.i(TAG, "launcher -> uri: $uri, mimeType: ${context.contentResolver.getType(uri!!)}")
        val cacheImageUri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", File(context.cacheDir, "tmp_img_${System.currentTimeMillis()}.jpg"))
        context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            context.contentResolver.openOutputStream(cacheImageUri)?.use { outputStream ->
                inputStream.copyTo(out = outputStream)
            }
        }
        cropLauncher.launch(cacheImageUri)
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "裁剪图片",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    if (permissions.any {
                            ContextCompat.checkSelfPermission(
                                context,
                                it
                            ) != PackageManager.PERMISSION_GRANTED
                        }) {
                        permissionsLauncher.launch(input = permissions)
                        return@clickable
                    }
                    val request = PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        .build()
                    launcher.launch(input = request, options = ActivityOptionsCompat.makeBasic())
                },
            color = Color.White
        )
        Image(
            bitmap = try {
                context.contentResolver.openInputStream(imageUri).use { inputStream: InputStream? ->
                    BitmapFactory.decodeStream(inputStream).asImageBitmap()
                }
            } catch (e: Exception) {
                Log.e(TAG, "CropScreen -> error: ${e.message}", e)
                0xFFBB11AA.toInt().toDrawable().toBitmap(width = 100, height = 100).asImageBitmap()
            },
            contentDescription = "裁剪图片结果",
            modifier = Modifier.fillMaxSize()
        )
    }

}