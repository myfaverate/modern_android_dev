package io.github.okhttplearn

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

private const val TAG: String = "MainApplication"

@HiltAndroidApp
internal class MainApplication internal constructor() : Application(){
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate...")
    }
}