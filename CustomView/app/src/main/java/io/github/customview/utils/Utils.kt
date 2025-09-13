package io.github.customview.utils

import android.util.Log

private const val TAG: String = "Utils"

internal class Utils internal constructor(){
    internal companion object {
        init {
            Log.i(TAG, "Utils companion object load...")
        }
    }
    internal fun hello(){
        Log.i(TAG, "Utils hello...")
    }
}