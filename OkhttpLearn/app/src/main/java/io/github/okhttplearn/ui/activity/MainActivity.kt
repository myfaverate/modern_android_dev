package io.github.okhttplearn.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import dagger.hilt.android.AndroidEntryPoint
import io.github.okhttplearn.ui.screen.NavScreen
import io.github.okhttplearn.ui.theme.OkhttpLearnTheme

private const val TAG: String = "MainActivity"

@AndroidEntryPoint
internal class MainActivity internal constructor(): ComponentActivity() {
    internal companion object {
        internal fun startActivity(context: Context){
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate...")
        enableEdgeToEdge()
        setContent {
            OkhttpLearnTheme {
                val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
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

