package edu.tyut.webviewlearn.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import edu.tyut.webviewlearn.route.Routes
import edu.tyut.webviewlearn.ui.theme.RoundedCornerShape10
import edu.tyut.webviewlearn.ui.theme.RoundedCornerShape5
import edu.tyut.webviewlearn.ui.theme.WebViewLearnTheme
import edu.tyut.webviewlearn.utils.NativeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG: String = "Greeting"

@Composable
internal fun Greeting(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState,
    navHostController: NavHostController,
) {
    var url: String by remember {
        mutableStateOf(value = "")
    }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    Column(
        modifier = modifier.verticalScroll(state = rememberScrollState())
    ) {
        TextField(
            value = url,
            onValueChange = { url = it },
            placeholder = {
                Text(text = "webViewUrl")
            }
        )
        Text(
            text = TAG, Modifier
                .background(color = Color.Blue)
                .clickable {
                    navHostController.navigate(route = Routes.WebView(url = url))
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("跳转成功")
                    }
                })
        Text(
            text = "录音",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    navHostController.navigate(route = Routes.Voice)
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("跳转成功")
                    }
                },
            color = Color.White
        )
        Text(
            text = "TTS服务",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    navHostController.navigate(route = Routes.TTS)
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("跳转成功")
                    }
                },
            color = Color.White
        )
        Text(
            text = "录屏",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    navHostController.navigate(route = Routes.VideoCapture)
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("跳转成功")
                    }
                },
            color = Color.White
        )

        Text(
            text = "Native",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(NativeUtils.stringFromJNI())
                    }
                },
            color = Color.White
        )


        Text(
            text = "Stream",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    navHostController.navigate(route = Routes.Stream)
                },
            color = Color.White
        )

        Text(
            text = "Store",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    navHostController.navigate(route = Routes.Store)
                },
            color = Color.White
        )

        Text(
            text = "Provider",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    navHostController.navigate(route = Routes.Provider)
                },
            color = Color.White
        )
        Text(
            text = "Service",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    navHostController.navigate(route = Routes.Service)
                },
            color = Color.White
        )
        Text(
            text = "Notify",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    navHostController.navigate(route = Routes.Notify)
                },
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    WebViewLearnTheme {
        val navHostController: NavHostController = rememberNavController()
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        Greeting(
            modifier = Modifier,
            navHostController = navHostController,
            snackBarHostState = snackBarHostState
        )
    }
}