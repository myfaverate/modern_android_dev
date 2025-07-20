package io.github.imagecrop.ui.screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityOptionsCompat
import io.github.imagecrop.bean.CropArgs
import io.github.imagecrop.contract.CropContract
import io.github.imagecrop.ui.theme.ImageCropTheme

private const val TAG: String = "Greeting"

@Composable
internal fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context: Context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = CropContract()
    ) {
        Log.i(TAG, "Greeting uri: $it")
    }
    val imageLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        // 权限问题会空指针
        launcher.launch(input = CropArgs(input = uri!!))
    }
    Text(
        text = "Hello $name!",
        modifier = modifier.clickable {
            val request = PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
            imageLauncher.launch(input = request, options = ActivityOptionsCompat.makeBasic())
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    ImageCropTheme {
        Greeting("Android")
    }
}