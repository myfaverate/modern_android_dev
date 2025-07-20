package io.github.imagecrop.bean

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class CropArgs(
    val input: Uri
): Parcelable
