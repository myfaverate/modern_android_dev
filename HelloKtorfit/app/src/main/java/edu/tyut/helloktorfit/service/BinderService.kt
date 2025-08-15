package edu.tyut.helloktorfit.service

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.lifecycle.LifecycleService
import edu.tyut.helloktorfit.utils.Utils
import java.lang.ref.WeakReference

private const val TAG: String = "BinderService"

internal class BinderService internal constructor() : LifecycleService() {

    private class MessengerHandler(private val context: WeakReference<Context>) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            context.get()?.let { context ->
                Log.i(TAG, "handleMessage -> message: ${msg.data}, processName: ${Utils.getProcessName(context)}, name: ${msg.data.getString("name")}, arg1: ${msg.arg1}, arg2: ${msg.arg2}, obj: ${msg.obj}")
            }
        }
    }

    private val handler: Handler by lazy {
        MessengerHandler(WeakReference<Context>(this))
    }

    private val mMessenger: Messenger by lazy {
        Messenger(handler)
    }

    override fun onBind(intent: Intent): IBinder? {
        val binder: IBinder? = super.onBind(intent)
        Log.i(TAG, "onBind -> binder: $binder, processName: ${Utils.getProcessName(this)}, name: ${intent.getStringExtra("name")}")
        return mMessenger.binder
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}