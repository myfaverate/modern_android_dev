package io.github.opensllearn.ui.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import io.github.opensllearn.ui.theme.OpenSLLearnTheme
import io.github.opensllearn.utils.Utils

private const val TAG: String = "Greeting"

@Composable
internal fun Greeting(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState
) {
    Text(
        text = "Hello zsh!",
        modifier = Modifier.clickable {
            Log.i(TAG, "Greeting...")
            Utils.hello1()
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    OpenSLLearnTheme {
        val navHostController: NavHostController = rememberNavController()
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        Greeting(navHostController = navHostController, snackBarHostState = snackBarHostState)
    }
}