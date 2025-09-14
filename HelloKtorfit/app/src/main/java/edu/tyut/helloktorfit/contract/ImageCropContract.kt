package edu.tyut.helloktorfit.contract

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import io.github.imagecrop.bean.CropArgs
import io.github.imagecrop.ui.activity.CropActivity

private const val TAG: String = "CropContract"

internal class ImageCropContract internal constructor(): ActivityResultContract<CropArgs, Uri>() {

    override fun createIntent(
        context: Context,
        input: CropArgs
    ): Intent {
        Log.i(TAG, "createIntent -> context: ${context.javaClass}, cropArgs: $input")
        return CropActivity.getCropActivityIntent(context = context, cropArgs = input)
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Uri {
        // Activity.RESULT_CANCELED // 用户取消
        Log.i(TAG, "parseResult -> resultCode: $resultCode, intent: $intent")
        return Uri.EMPTY
    }

}