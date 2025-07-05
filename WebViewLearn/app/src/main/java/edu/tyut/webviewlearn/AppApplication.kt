package edu.tyut.webviewlearn

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

private const val TAG: String = "AppApplication"

@HiltAndroidApp
internal class AppApplication internal constructor(): Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate...")
    }
}