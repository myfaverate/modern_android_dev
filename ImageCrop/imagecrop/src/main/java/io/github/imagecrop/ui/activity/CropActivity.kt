package io.github.imagecrop.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import io.github.imagecrop.bean.CropArgs
import io.github.imagecrop.ui.screen.CropScreen
import io.github.imagecrop.ui.theme.ImageCropTheme

private const val CROP_ARGS_KEY: String = "cropArgsKey"

class CropActivity internal constructor() : ComponentActivity() {
     companion object {
         fun getCropActivityIntent(context: Context, cropArgs: CropArgs): Intent {
             return Intent(context, CropActivity::class.java)
                 .putExtra(CROP_ARGS_KEY, cropArgs) // Parcelable
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 获取 uri
        val cropArgs: CropArgs =
            IntentCompat.getParcelableExtra<CropArgs>(intent, CROP_ARGS_KEY, CropArgs::class.java) ?: CropArgs(input = Uri.EMPTY)
        setContent{
            ImageCropTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CropScreen(
                        cropArgs = cropArgs,
                        modifier = Modifier.padding(paddingValues = innerPadding)
                    )
                }
            }
        }
    }
}
