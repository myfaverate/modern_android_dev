package edu.tyut.login.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.tyut.login.data.remote.bean.Person
import edu.tyut.login.ui.state.UiState
import edu.tyut.login.ui.theme.Pink40
import edu.tyut.login.viewmodel.LoginViewModel

private const val TAG: String = "LoginScreen"

@Composable
internal fun LoginScreen(
    name: String, modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel<LoginViewModel>()
) {
    val personState: UiState<Person> by loginViewModel.personState.collectAsStateWithLifecycle()
    var personStr: String by remember { mutableStateOf("") }
    LaunchedEffect(Unit){
        Log.i(TAG, "LoginScreen...")
        loginViewModel.login()
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
                personStr = person.toString()
            }
            is UiState.Error -> {
                val message: String = (personState as UiState.Error).exception.message.toString()
                Log.i(TAG, "Greeting -> message: $message")
            }
        }
    }
    
    Text(
        textAlign = TextAlign.Center,
        text = "Login $personStr!",
        modifier = modifier.fillMaxWidth()
            .background(color = Pink40, shape = RoundedCornerShape(16.dp)).padding(vertical = 16.dp)

    )
}
