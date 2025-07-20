package edu.tyut.helloktorfit

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

private const val TAG: String = "MainApplication"

@HiltAndroidApp
internal class MainApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate...")
        System.setProperty(kotlinx.coroutines.DEBUG_PROPERTY_NAME, if (BuildConfig.DEBUG) kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON else kotlinx.coroutines.DEBUG_PROPERTY_VALUE_OFF)
    }
}