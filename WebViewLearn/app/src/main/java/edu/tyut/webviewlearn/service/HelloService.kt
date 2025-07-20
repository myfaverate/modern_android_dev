package edu.tyut.webviewlearn.service

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import kotlin.random.Random

private const val TAG: String = "HelloService"

internal class HelloService internal constructor() : LifecycleService() {

    companion object {
        internal fun bindService(context: Context, connection: ServiceConnection){
            val isSuccess: Boolean = context.bindService(Intent(context, HelloService::class.java), connection, Context.BIND_AUTO_CREATE)
            Log.i(TAG, "bindService isSuccess: $isSuccess")
        }
        internal fun unbindService(context: Context, connection: ServiceConnection) {
            context.unbindService(connection)
        }
        internal fun startService(context: Context) {
            context.startService(Intent(context, HelloService::class.java))
        }
        internal fun stopService(context: Context) {
            context.stopService(Intent(context, HelloService::class.java))
        }
    }

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
        return true
        // return super.onUnbind(intent).apply {
        //     Log.i(TAG, "onUnbind -> intent: $intent value: $this")
        // }
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy...")
        super.onDestroy()
    }

    internal class HelloBinder : Binder() {
        internal fun getHello(): String {
            Log.i(TAG, "getHello -> current: ${Thread.currentThread()}")
            return "Hello: ${Random.nextInt(until = 1000)}"
        }
    }
}