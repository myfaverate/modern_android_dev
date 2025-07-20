package edu.tyut.helloktorfit.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import edu.tyut.helloktorfit.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val TAG: String = "HelloService"
private const val CHANNEL_ID: String = "channelId"
private const val NOTIFICATION_ID: Int = 0x0000_0001
private const val REQUEST_CODE: Int = 0x0000_0002
private const val ACTION_UPDATE_KEY: String = "updateKey"
private const val ACTION_UPDATE: Int = 0x0000_0003

internal class HelloService internal constructor() : LifecycleService() {
    internal companion object {
        internal fun getServiceIntent(context: Context): Intent {
            return Intent(context, HelloService::class.java)
        }
    }
    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand intent: $intent, flags: $flags, startId: $startId")
        if (intent?.getIntExtra(ACTION_UPDATE_KEY, 0) == ACTION_UPDATE){
            val notification: Notification = getNotification(content = "通知: ${Random.nextInt(until = 10000_0000)}")
            lifecycleScope.launch {
                delay(5000)
                updateNotification(notification)
            }
            return super.onStartCommand(intent, flags, startId)
        }
        val channel: NotificationChannelCompat = NotificationChannelCompat.Builder(CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_MAX)
            .setName("Hello Channel")
            .build()

        Log.i(TAG, "onStartCommand -> lockscreenVisibility: ${channel.lockscreenVisibility}")
        notificationManager.createNotificationChannel(channel)
        val notification: Notification = getNotification(content = "Hello World")
        lifecycleScope.launch {
            delay(5000)
            updateNotification(notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun getNotification(content: String): Notification {
        val updatePendingIntent: PendingIntent? =
            PendingIntentCompat.getService(
                this, REQUEST_CODE,
                getServiceIntent(this).putExtra(ACTION_UPDATE_KEY, ACTION_UPDATE), PendingIntent.FLAG_UPDATE_CURRENT, false
            )
        updatePendingIntent
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Hello 通知")
            .setContentText(content)
            .addAction(NotificationCompat.Action.Builder(IconCompat.createWithResource(this, R.drawable.ic_launcher_background), "更新", updatePendingIntent).build())
            .build()
        return notification
    }

    private fun updateNotification(notification: Notification){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy...")
    }
}