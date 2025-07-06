package edu.tyut.webviewlearn.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import kotlin.random.Random

private const val TAG: String = "HelloService"

internal class HelloService internal constructor() : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate...")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId).apply {
            Log.i(TAG, "onStartCommand intent: $intent, flags: $flags, startId: $startId value: $this")
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        Log.i(TAG, "onBind intent: $intent...")
        return HelloBinder()
    }

    override fun onRebind(intent: Intent?) {
        Log.i(TAG, "onRebind -> intent: $intent")
        super.onRebind(intent)
    }
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent).apply {
            Log.i(TAG, "onUnbind -> intent: $intent value: $this")
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy...")
        super.onDestroy()
    }

    internal class HelloBinder : Binder() {
        internal fun getHello(): String {
            return "Hello: ${Random.nextInt(until = 1000)}"
        }
    }
}