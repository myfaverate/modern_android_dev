package edu.tyut.appstarter.initializer

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.tencent.mmkv.MMKV

private const val TAG: String = "MMKVInitializer"

class MMKVInitializer : Initializer<Unit>{
    override fun create(context: Context) {
        val rootDir: String = MMKV.initialize(context)
        Log.i(TAG, "create -> rootDir: $rootDir")
    }

    override fun dependencies(): List<Class<out Initializer<*>?>?> {
        return emptyList()
    }
}