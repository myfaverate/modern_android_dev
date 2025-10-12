package io.github.imagecrop.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.Keep
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.IntentCompat
import io.github.imagecrop.bean.CropArgs
import io.github.imagecrop.ui.screen.CropScreen
import io.github.imagecrop.ui.theme.ImageCropTheme
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

private const val TAG: String = "CropActivity"
private const val CROP_ARGS_KEY: String = "cropArgsKey"

@Keep
class CropActivity internal constructor() : ComponentActivity() {

    @Keep
    companion object {

        const val CROP_FAILURE: Int = RESULT_FIRST_USER + 0

        /**
         * Companion 类会被混淆，需要注意
         */
        @JvmStatic
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
            IntentCompat.getParcelableExtra<CropArgs>(intent, CROP_ARGS_KEY, CropArgs::class.java)
                ?: CropArgs(input = Uri.EMPTY, output = Uri.EMPTY)
        Log.i(TAG, "onCreate -> cropArgs: $cropArgs")
        require(value = cropArgs.aspectRatio.size == 2){
            "aspectRatio must have length 2"
        }
        require(value = cropArgs.aspectRatio.any { it >= 0 }){
            "aspectRatio value must more then equals 0"
        }
        require(value = cropArgs.maxResultSize.size == 2){
            "maxResultSize must have length 2"
        }
        require(value = cropArgs.maxResultSize.any { it >= 0 }){
            "maxResultSize value must more then equals 0"
        }
        setContent {
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
