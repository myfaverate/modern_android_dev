package edu.tyut.helloktorfit.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.activity.compose.ManagedActivityResultLauncher
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import edu.tyut.helloktorfit.ui.theme.RoundedCornerShape10
import edu.tyut.helloktorfit.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
internal fun SystemScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
){
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val launcher: ManagedActivityResultLauncher<String, Boolean> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isSuccess: Boolean ->
        coroutineScope.launch {
            snackBarHostState.showSnackbar("成功: $isSuccess")
        }
    }
    launcher
    Column(
        modifier = Modifier.fillMaxSize()
    ){
        Text(
            text = "发送信息",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        launcher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        return@clickable
                    }
                    Utils.sha256sum(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "video1.mp4").absolutePath)
                },
            color = Color.White
        )
    }
}