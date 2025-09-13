package io.github.customview

import android.app.Application
import android.content.Context
import android.util.Log

private const val TAG: String = "MainApplication"

internal class MainApplication internal constructor() : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate...")
    }
}