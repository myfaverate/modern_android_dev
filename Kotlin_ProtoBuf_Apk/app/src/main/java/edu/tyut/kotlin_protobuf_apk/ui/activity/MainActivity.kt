package edu.tyut.kotlin_protobuf_apk.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.tyut.kotlin_protobuf_apk.data.remote.bean.Person
import edu.tyut.kotlin_protobuf_apk.ui.state.UiState
import edu.tyut.kotlin_protobuf_apk.ui.theme.Kotlin_ProtoBuf_ApkTheme
import edu.tyut.kotlin_protobuf_apk.viewmodel.HelloViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

private const val TAG: String = "MainActivity"

@AndroidEntryPoint
internal class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // val helloViewModel: HelloViewModel = viewModel<HelloViewModel>()
            // LaunchedEffect(Unit) {
            //     helloViewModel.login()
            //     Person.serializer()
            // }
            // val personState: UiState<Person> by helloViewModel.personState.collectAsStateWithLifecycle()
            // LaunchedEffect(personState) {
            //     when(personState){
            //         is UiState.IDLE -> {
            //             Log.i(TAG, "Greeting idle...")
            //         }
            //         is UiState.Loading -> {
            //             Log.i(TAG, "Greeting loading...")
            //         }
            //         is UiState.Success -> {
            //             val person: Person = (personState as UiState.Success<Person>).data
            //             Log.i(TAG, "Greeting -> person: $person")
            //         }
            //         is UiState.Error -> {
            //             val message: String = (personState as UiState.Error).exception.message.toString()
            //             Log.i(TAG, "Greeting -> message: $message")
            //         }
            //     }
            // }
            Kotlin_ProtoBuf_ApkTheme {
                Navigation()
            }
        }
    }
}

@Composable
private fun Navigation() {
    // Create the NavController
    val navController: NavHostController = rememberNavController()
    // Set up the navigation graph
    NavHost(navController = navController, startDestination = "Greeting") {
        composable("Greeting") {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Greeting(
                    name = "Android",
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun Greeting(
    name: String, modifier: Modifier = Modifier,
    helloViewModel: HelloViewModel = hiltViewModel<HelloViewModel>()
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val personState: UiState<Person> by helloViewModel.personState.collectAsStateWithLifecycle()
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
        text = "Hello $name!",
        modifier = modifier.clickable{
            coroutineScope.launch{
                helloViewModel.login()
                helloViewModel.savePerson(Person(name = "张书豪水水水水", age = 18, gender = "男"))
                val person: Person? = helloViewModel.readPerson().firstOrNull()
                Log.i(TAG, "Greeting -> person: $person")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    Kotlin_ProtoBuf_ApkTheme {
        Greeting("Android")
    }
}