package edu.tyut.helloktorfit.ui.screen

import android.app.Notification
import android.app.NotificationChannel
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
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
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import edu.tyut.helloktorfit.R
import edu.tyut.helloktorfit.service.DownloadService
import edu.tyut.helloktorfit.service.HelloService
import edu.tyut.helloktorfit.service.MusicService
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape10
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG: String = "ServiceScreen"

@OptIn(UnstableApi::class)
@Composable
internal fun ServiceScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
){
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { it ->
        coroutineScope.launch {
            snackBarHostState.showSnackbar("获取权限${if (it.values.all { it }) "成功" else "失败"}")
        }
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ){
        Text(
            text = "创建通知",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                            permissionLauncher.launch(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS))
                            return@clickable
                        }
                    }
                    ContextCompat.startForegroundService(context, MusicService.getServiceIntent(context))
                },
            color = Color.White
        )
        Text(
            text = "断开Service",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                },
            color = Color.White
        )
    }
}