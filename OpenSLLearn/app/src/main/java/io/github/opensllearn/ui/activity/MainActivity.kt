package io.github.opensllearn.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.opensllearn.ui.screen.Greeting
import io.github.opensllearn.ui.screen.NavScreen
import io.github.opensllearn.ui.theme.OpenSLLearnTheme

internal class MainActivity internal constructor(): ComponentActivity() {

    internal companion object {
        @JvmStatic
        internal fun startActivity(context: Context){
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenSLLearnTheme {
                val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
                ) { innerPadding: PaddingValues ->
                    NavScreen(
                        modifier = Modifier.padding(paddingValues = innerPadding),
                        snackBarHostState = snackBarHostState
                    )
                }
            }
        }
    }
}

