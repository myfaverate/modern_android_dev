package io.github.okhttplearn.ui.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import io.github.okhttplearn.ui.theme.OkhttpLearnTheme

private const val TAG: String = "Greeting"

@Composable
internal fun Greeting(
    message: String,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onNavigationToHome: () -> Unit,
) {
    LaunchedEffect(key1 = Unit) {
        Log.i(TAG, "Greeting -> message: $message")
    }
    Text(
        text = TAG,
        fontSize = 30.sp,
        modifier = modifier.clickable(onClick = onNavigationToHome)
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    OkhttpLearnTheme {
        val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
        Greeting(message = "Android", modifier = Modifier, snackBarHostState = snackBarHostState){
            Log.i(TAG, "GreetingPreview click...")
        }
    }
}