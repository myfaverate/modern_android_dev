package edu.tyut.helloktorfit.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import edu.tyut.helloktorfit.ui.screen.NavScreen
import edu.tyut.helloktorfit.ui.theme.HelloKtorfitTheme
import edu.tyut.helloktorfit.viewmodel.HelloViewModel

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {
    val a by viewModels<HelloViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelloKtorfitTheme {
                val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackBarHostState)
                    }
                ) { innerPadding: PaddingValues ->
                    NavScreen(
                        modifier = Modifier.padding(paddingValues = innerPadding),
                        snackBarHostState = snackBarHostState,
                    )
                }
            }
        }
    }
}