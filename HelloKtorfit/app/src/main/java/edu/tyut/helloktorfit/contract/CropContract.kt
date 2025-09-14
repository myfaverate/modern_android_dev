package edu.tyut.helloktorfit.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import io.github.imagecrop.ui.activity.CropActivity
import java.io.File

private const val TAG: String = "CropContract"

internal class CropContract internal constructor(
    private val context: Context
): ActivityResultContract<Uri, Uri?>() {

    private val outputUri = FileProvider.getUriForFile(
        context, "${context.packageName}.provider", File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "${System.currentTimeMillis()}.jpg"
        )
    )

    override fun createIntent(
        context: Context,
        input: Uri
    ): Intent {
        val intent = Intent("com.android.camera.action.CROP").setDataAndType(input, "image/*")
            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            .putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            .putExtra("aspectX", 1)
            .putExtra("aspectY", 1)
            .putExtra("return-data", false)

        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .forEach { resolveInfo: ResolveInfo ->
                Log.i(TAG, "PhotoCard -> packageName: ${resolveInfo.activityInfo.packageName}")
                context.grantUriPermission(
                    resolveInfo.activityInfo.packageName,
                    outputUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }


        return intent
    }


    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Uri? {
        Log.i(TAG, "parseResult -> resultCode: $resultCode, intent: $intent, outputUri: $outputUri")
        intent?.extras?.keySet()?.forEach {
            Log.i(TAG, "parseResult -> key: $it, value: ${intent.extras?.get(it)}")
        }
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            intent.data?.apply {
                context.revokeUriPermission(this, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } ?: run {
                context.revokeUriPermission(outputUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                outputUri
            }
        } else {
            null
        }
    }
}