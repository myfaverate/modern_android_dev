package io.github.customview

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

private const val TAG: String = "MainApplication"

internal class MainApplication internal constructor() : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate...")
    }
}