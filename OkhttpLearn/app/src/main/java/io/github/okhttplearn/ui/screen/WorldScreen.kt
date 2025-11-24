package io.github.okhttplearn.ui.screen

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.okhttplearn.ui.theme.OkhttpLearnTheme
import io.github.okhttplearn.utils.Utils
import java.io.File
import kotlin.time.measureTime

private const val TAG: String = "WorldScreen"

@Composable
internal fun WorldScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
) {
    val context: Context = LocalContext.current
    val launcher: ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.RequestMultiplePermissions()
        ) { map ->
            Log.i(TAG, "WorldScreen -> map: $map")
        }
    Column(modifier = modifier) {
        Text(
            text = "写文件", modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .background(
                    color = Color(
                        color = 0xFFF8BBD0
                    ),
                    shape = RoundedCornerShape(size = 5.dp)
                )
                .padding(all = 5.dp)
                .clickable {
                    val zipFile = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        "mysql-connector-c++-9.5.0-src.zip"
                    )
                    measureTime {
                        Utils.unzip(zipFile, zipFile.parentFile!!)
                    }.let { duration ->
                        Log.i(TAG, "WorldScreen -> duration: ${duration.inWholeSeconds}s")
                    }
                }
        )
        Text(
            text = "压缩zip", modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .background(
                    color = Color(
                        color = 0xFFF8BBD0
                    ),
                    shape = RoundedCornerShape(size = 5.dp)
                )
                .padding(all = 5.dp)
                .clickable {
                    launcher.launch(
                        arrayOf(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
        )
        Text(
            text = "压缩gzip", modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .background(
                    color = Color(
                        color = 0xFFF8BBD0
                    ),
                    shape = RoundedCornerShape(size = 5.dp)
                )
                .padding(all = 5.dp)
        )
        Text(
            text = "计算md5", modifier = Modifier
                .fillMaxWidth()
                .padding(all = 5.dp)
                .background(
                    color = Color(
                        color = 0xFFF8BBD0
                    ),
                    shape = RoundedCornerShape(size = 5.dp)
                )
                .padding(all = 5.dp)
                .clickable {
                }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorldScreenPreview() {
    OkhttpLearnTheme {
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        WorldScreen(modifier = Modifier, snackBarHostState = snackBarHostState)
    }
}