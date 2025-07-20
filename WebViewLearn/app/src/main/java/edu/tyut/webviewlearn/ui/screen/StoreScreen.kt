package edu.tyut.webviewlearn.ui.screen

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Paint
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.contentValuesOf
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import edu.tyut.webviewlearn.ui.theme.Purple40
import edu.tyut.webviewlearn.ui.theme.RoundedCornerShape10
import edu.tyut.webviewlearn.utils.broadcastAsFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.OutputStream

private const val TAG: String = "StoreScreen"

@OptIn(markerClass = [UnstableApi::class])
@Composable
internal fun StoreScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState
) {
    val context: Context = LocalContext.current
    val intents: Intent by context.broadcastAsFlow(Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON).collectAsStateWithLifecycle(initialValue = Intent())
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val imagePermissions: Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { map: Map<String, Boolean> ->
            val isSuccess: Boolean = map.all { it.value }
            coroutineScope.launch {
                snackBarHostState.showSnackbar("获取权限${if (isSuccess) "成功" else "失败"}")
            }
        }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "存储图片",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    if (
                        imagePermissions.any {
                            ContextCompat.checkSelfPermission(
                                context,
                                it
                            ) != PackageManager.PERMISSION_GRANTED
                        }
                    ) {
                        launcher.launch(input = imagePermissions)
                        return@clickable
                    }
                    val bitmap: Bitmap = createBitmap(100, 100).applyCanvas {
                        val paint = Paint()
                        paint.isAntiAlias = true

                        paint.color = Color(0xFFE0C2CC).toArgb()
                        drawRoundRect(0F, 0F, 100F, 100F, 0F, 0F, paint)

                        paint.color = Purple40.toArgb()
                        paint.textSize = 12F
                        drawText("Hello World 世界! ", 0F, paint.textSize, paint)
                    }
                    val contentValues: ContentValues = contentValuesOf(
                        MediaStore.Images.Media.DISPLAY_NAME to "${System.currentTimeMillis()}.png",
                        MediaStore.Images.Media.MIME_TYPE to MimeTypes.IMAGE_PNG
                    )
                    // 存在 /sdcard
                    context.contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )?.let { uri: Uri ->
                        Log.i(TAG, "StoreScreen -> uri: $uri")
                        context.contentResolver.openOutputStream(uri)
                            ?.use { outputStream: OutputStream ->
                                val isSuccess: Boolean =
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                Log.i(TAG, "StoreScreen -> isSuccess: $isSuccess")
                            }
                    }
                },
            color = Color.White
        )
        Text(
            text = "查看图片",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        arrayOf(MediaStore.Images.Media._ID),
                        null,
                        null,
                        null
                    )?.use { cursor: Cursor ->
                        val idColumn: Int = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                        while (cursor.moveToNext()) {
                            val id: Long = cursor.getLong(idColumn)
                            val contentUri: Uri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                id
                            )
                            Log.i(TAG, "StoreScreen -> contentUri: $contentUri")
                        }
                    }
                    coroutineScope.launch {
                        context.broadcastAsFlow(Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON)
                            .collect { intent: Intent ->
                                Log.i(TAG, "StoreScreen -> 屏幕关闭开启 intent: $intent")
                            }
                    }
                },
            color = Color.White
        )
    }
}

@Preview
@Composable
private fun StoreScreenPreview() {
    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
    StoreScreen(modifier = Modifier, snackBarHostState = snackBarHostState)
}