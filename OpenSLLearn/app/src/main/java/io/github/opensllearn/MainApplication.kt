package io.github.opensllearn

import android.app.Application
import android.content.Context
import android.util.Log

private const val TAG: String = "MainApplication"

internal class MainApplication internal constructor() : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Log.i(TAG, "attachBaseContext base: ${base?.javaClass}")
    }
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate context: $javaClass")
    }
}