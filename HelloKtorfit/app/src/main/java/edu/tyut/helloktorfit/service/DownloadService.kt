package edu.tyut.helloktorfit.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.PendingIntentCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import edu.tyut.helloktorfit.R
import edu.tyut.helloktorfit.data.remote.repository.HelloRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

private const val TAG: String = "DownloadService"
private const val CHANNEL_ID = "downloadChannel"
private const val NOTIFICATION_ID: Int = 0x0000_0002
private const val REQUEST_CODE: Int = 0x0000_0002
private const val ACTION_UPDATE_KEY: String = "updateKey"
private const val ACTION_UPDATE: Int = 0x0000_0003

internal class DownloadService internal constructor() : LifecycleService() {
    internal companion object {
        internal fun getServiceIntent(context: Context): Intent {
            return Intent(context, DownloadService::class.java)
        }
    }

    private val notificationManager: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(this)
    }
    private val helloRepository: HelloRepository by lazy {
        EntryPointAccessors.fromApplication(
            context = this,
            entryPoint = DownloadServiceEntryPoint::class.java
        ).helloRepository()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand helloRepository: $helloRepository")
        if (intent?.getIntExtra(ACTION_UPDATE_KEY, 0) == ACTION_UPDATE) {
            val fileName = "新建文件夹.zip" // nacos-server-3.0.2.zip
            val output: Uri = FileProvider.getUriForFile(
                this, "${packageName}.provider", File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )
            )
            lifecycleScope.launch {
                helloRepository.download(context = this@DownloadService, fileName = fileName, output = output){
                    val notification: Notification = createNotification(progress = it)
                    updateNotification(notification)
                }
            }
            // val notification: Notification = createNotification(progress = Random.nextInt(until = 100))
            // updateNotification(notification)
            // Log.i(TAG, "onStartCommand -> thread: ${Thread.currentThread()}")
            return super.onStartCommand(intent, flags, startId)
        }
        val channel: NotificationChannelCompat = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_MAX
        )
            .setName("下载 Channel")
            .build()
        notificationManager.createNotificationChannel(channel)
        val notification: Notification = createNotification(progress = 0)
        updateNotification(notification)
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification(progress: Int): Notification {
        val updatePendingIntent: PendingIntent? =
            PendingIntentCompat.getService(
                this,
                REQUEST_CODE,
                getServiceIntent(this)
                    .putExtra(ACTION_UPDATE_KEY, ACTION_UPDATE),
                PendingIntent.FLAG_UPDATE_CURRENT,
                false
            )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("下载 通知")
            .setProgress(100, progress, false)
            .setOnlyAlertOnce(true)
            .addAction(
                NotificationCompat.Action.Builder(
                    IconCompat.createWithResource(
                        this,
                        R.drawable.ic_launcher_background
                    ), "下载更新", updatePendingIntent
                ).build()
            )
            .build()
        return notification
    }

    private fun updateNotification(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }
}

@EntryPoint
@InstallIn(value = [SingletonComponent::class])
private interface DownloadServiceEntryPoint {
    fun helloRepository(): HelloRepository
}
