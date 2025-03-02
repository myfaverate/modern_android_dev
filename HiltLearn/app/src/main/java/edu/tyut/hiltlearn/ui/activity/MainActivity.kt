package edu.tyut.hiltlearn.ui.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import dagger.hilt.android.AndroidEntryPoint
import edu.tyut.hiltlearn.data.remote.bean.Person
import edu.tyut.hiltlearn.di.bean.Cat
import edu.tyut.hiltlearn.di.bean.Truck
import edu.tyut.hiltlearn.provider.InitProvider
import edu.tyut.hiltlearn.ui.state.UiState
import edu.tyut.hiltlearn.ui.theme.HiltLearnTheme
import edu.tyut.hiltlearn.ui.theme.Pink40
import edu.tyut.hiltlearn.viewmodel.HelloViewModel
import edu.tyut.login.di.bean.LoginUser
import edu.tyut.login.ui.activity.LoginActivity
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG: String = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    internal lateinit var truck: Truck
    @Inject
    internal lateinit var catFactory : Cat.Factory
    @Inject
    internal lateinit var loginUserFactory: LoginUser.Factory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiltLearnTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
        Log.i(TAG, "onCreate -> truck: ${truck.deliver()}, cat: ${catFactory.create("Tom")}, loginUser： ${loginUserFactory.create("张书豪")}}")
    }
}

@Composable
private fun Greeting(
    name: String, modifier: Modifier = Modifier,
    viewModel: HelloViewModel = viewModel<HelloViewModel>(),
) {
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val personState: UiState<Person> by viewModel.personState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        context.contentResolver.query(InitProvider.CONTENT_URI, null, null, null, null)?.close()
    }
    LaunchedEffect(personState) {
        when(personState){
            is UiState.IDLE -> {
                Log.i(TAG, "Greeting idle...")
            }
            is UiState.Loading -> {
                Log.i(TAG, "Greeting loading...")
            }
            is UiState.Success -> {
                val person: Person = (personState as UiState.Success<Person>).data
                Log.i(TAG, "Greeting -> person: $person")
            }
            is UiState.Error -> {
                val message: String = (personState as UiState.Error).exception.message.toString()
                Log.i(TAG, "Greeting -> message: $message")
            }
        }
    }
    Text(
        textAlign = TextAlign.Center,
        text = "Hello $name!",
        modifier = modifier.fillMaxWidth().background(color = Pink40, shape = RoundedCornerShape(16.dp)).padding(vertical = 16.dp)
            .clickable{
                coroutineScope.launch{
                    viewModel.getPerson()
                    LoginActivity.startActivity(context = context)
                }
            }
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    HiltLearnTheme {
        Greeting(name = "Android")
    }
}