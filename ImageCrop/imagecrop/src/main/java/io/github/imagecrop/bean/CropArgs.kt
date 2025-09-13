package io.github.imagecrop.bean

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class CropArgs(
    val input: Uri,
    val output: Uri,
    val aspectRatio: IntArray = intArrayOf(0, 0),
    val maxResultSize: IntArray = intArrayOf(1920, 1920),
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CropArgs

        if (input != other.input) return false
        if (output != other.output) return false
        if (!aspectRatio.contentEquals(other.aspectRatio)) return false
        if (!maxResultSize.contentEquals(other.maxResultSize)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = input.hashCode()
        result = 31 * result + output.hashCode()
        result = 31 * result + aspectRatio.contentHashCode()
        result = 31 * result + maxResultSize.contentHashCode()
        return result
    }
}
/*
    .withAspectRatio(16, 9)
    .withMaxResultSize(maxWidth, maxHeight)
 */