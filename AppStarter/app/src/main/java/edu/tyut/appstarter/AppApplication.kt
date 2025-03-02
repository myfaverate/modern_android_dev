package edu.tyut.appstarter

import android.app.Application
import android.content.Context
import android.util.Log

private const val TAG: String = "AppApplication"

class AppApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Log.i(TAG, "attachBaseContext...")
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate...")
    }
}