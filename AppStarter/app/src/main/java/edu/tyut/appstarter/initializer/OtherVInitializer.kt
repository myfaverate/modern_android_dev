package edu.tyut.appstarter.initializer

import android.content.Context
import android.util.Log
import androidx.startup.Initializer


private const val TAG: String = "OtherVInitializer"

class OtherVInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        Log.i(TAG, "create...")
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}