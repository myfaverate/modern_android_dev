package edu.tyut.webviewlearn.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import edu.tyut.webviewlearn.route.Routes
import edu.tyut.webviewlearn.ui.theme.WebViewLearnTheme
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
        modifier = modifier
    ){
        TextField(
            value = url,
            onValueChange = { url = it },
            placeholder = {
                Text(text = "webViewUrl")
            }
        )
        Text(text = TAG, Modifier
            .background(color = Color.Blue)
            .clickable {
                navHostController.navigate(route = Routes.WebView(url = url))
                coroutineScope.launch {
                    snackBarHostState.showSnackbar("跳转成功")
                }
            })
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