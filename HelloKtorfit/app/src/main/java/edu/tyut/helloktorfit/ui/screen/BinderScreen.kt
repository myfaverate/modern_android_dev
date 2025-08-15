package edu.tyut.helloktorfit.ui.screen

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavHostController
import edu.tyut.helloktorfit.service.BinderService
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape10

private const val TAG: String = "BinderScreen"

@Composable
internal fun BinderScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
){
    var isBinder: Boolean by remember {
        mutableStateOf(value = false)
    }
    val context: Context = LocalContext.current
    var mMessenger: Messenger? by remember {
        mutableStateOf(value = null)
    }
    val connection: ServiceConnection by remember {
        mutableStateOf(value = object : ServiceConnection{
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                mMessenger = Messenger(service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.i(TAG, "onServiceDisconnected name: $name")
            }
        })
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            if (isBinder){
                context.unbindService(connection)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ){
        Text(
            text = "绑定服务",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    isBinder = true
                    val intent: Intent = Intent(context, BinderService::class.java)
                        .putExtra("name", "binderScreen")
                    context.bindService(intent, connection, Service.BIND_AUTO_CREATE)
                },
            color = Color.White
        )
        Text(
            text = "发送信息",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    val message: Message = Message.obtain()
                    // 1,048,576
                    message.data = bundleOf("name" to "z".repeat(n = 200080))
                    message.arg1 = 11
                    message.arg2 = 22
                    mMessenger?.send(message)
                },
            color = Color.White
        )
    }
}