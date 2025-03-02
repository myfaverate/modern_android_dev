package edu.tyut.login.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import edu.tyut.login.ui.screen.LoginScreen
import edu.tyut.login.ui.theme.HiltLearnTheme

private const val TAG: String = "LoginActivity"

@AndroidEntryPoint
class LoginActivity : ComponentActivity(){
    companion object{
        fun startActivity(context: Context){
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.i(TAG, "onCreate...")
        setContent {
            HiltLearnTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(name = "Login", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}