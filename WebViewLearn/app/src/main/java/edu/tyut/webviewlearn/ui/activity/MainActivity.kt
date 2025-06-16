package edu.tyut.webviewlearn.ui.activity

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import edu.tyut.webviewlearn.ui.screen.NavScreen
import edu.tyut.webviewlearn.ui.theme.WebViewLearnTheme

@AndroidEntryPoint
internal class MainActivity internal constructor(): ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebViewLearnTheme {
                val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackBarHostState)
                    }
                ) { innerPadding: PaddingValues ->
                    NavScreen(
                        modifier = Modifier.padding(innerPadding),
                        snackBarHostState = snackBarHostState
                    )
                }
            }
        }
    }
}