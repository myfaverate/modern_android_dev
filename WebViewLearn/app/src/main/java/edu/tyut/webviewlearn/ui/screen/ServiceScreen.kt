package edu.tyut.webviewlearn.ui.screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.tyut.webviewlearn.service.HelloService
import edu.tyut.webviewlearn.ui.theme.RoundedCornerShape10
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG: String = "ProviderScreen"

@Composable
internal fun ServiceScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState
) {
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    var helloBinder: HelloService.HelloBinder? by remember {
        mutableStateOf(value = null)
    }
    val connection: ServiceConnection by remember {
        mutableStateOf(value = object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                helloBinder = service as? HelloService.HelloBinder
                Log.i(TAG, "onServiceConnected -> name: $name, service: $service")
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.i(TAG, "onServiceDisconnected -> name: $name")
            }
        })
    }
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "启动服务",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    // context.startService(Intent(context, HelloService::class.java))
                    HelloService.bindService(context, connection)
                    HelloService.startService(context)
                    val message = helloBinder?.getHello()
                    Log.i(TAG, "ServiceScreen -> message: $message")
                },
            color = Color.White
        )

        Text(
            text = "关闭服务",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    // context.stopService(Intent(context, HelloService::class.java))
                    context.unbindService(connection)
                },
            color = Color.White
        )
    }
}
