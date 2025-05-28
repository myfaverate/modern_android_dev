package edu.tyut.helloktorfit.ui.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import edu.tyut.helloktorfit.data.bean.Person
import edu.tyut.helloktorfit.route.Routes
import edu.tyut.helloktorfit.ui.theme.HelloKtorfitTheme
import edu.tyut.helloktorfit.viewmodel.HelloViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG: String = "Greeting"

@Composable
internal fun Greeting(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
    helloViewModel: HelloViewModel = hiltViewModel<HelloViewModel>()
) {
    var content: String by remember {
        mutableStateOf("")
    }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    Text(
        text = "Hello Greeting $content",
        modifier = modifier.clickable {
            // navHostController.navigate(Routes.Hello(name = "Hello"))
            coroutineScope.launch {
                val hello: String = helloViewModel.getHello()
                val person: Person = helloViewModel.getPerson(person = Person(name = "小时候", age = 18, gender = "男生"))
                content = "hello: $hello, person: $person"
                Log.i(TAG, "Greeting -> hello: $hello, person: $person")
                snackBarHostState.showSnackbar(hello)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    HelloKtorfitTheme {
        val navHostController: NavHostController = rememberNavController()
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        Greeting(navHostController = navHostController, snackBarHostState = snackBarHostState)
    }
}