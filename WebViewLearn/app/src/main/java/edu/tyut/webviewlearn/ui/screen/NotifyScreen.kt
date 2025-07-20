package edu.tyut.webviewlearn.ui.screen

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.tyut.webviewlearn.R
import edu.tyut.webviewlearn.ui.theme.RoundedCornerShape10
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG: String = "HelloScreen"

@Composable
internal fun NotifyScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState
){
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isSuccess ->
        coroutineScope.launch {
            snackBarHostState.showSnackbar("权限获取是否成功: $isSuccess")
        }
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "发送通知",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        return@clickable
                    }
                    val channelId = "channelId"
                    val notificationId = 0x7FFFFFFF
                    val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)
                    val channel: NotificationChannelCompat = NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_NONE)
                        .setName("HelloChannel")
                        .setDescription("HelloChannel 渠道描述")
                        .build()
                    notificationManager.createNotificationChannel(channel)
                    val notification: Notification = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Title")
                        .setContentText("Hello World")
                        .build()
                    coroutineScope.launch {
                        delay(3000)
                        notificationManager.notify(notificationId, notification)
                    }
                },
            color = Color.White
        )
    }
}