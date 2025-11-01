package io.github.okhttplearn.ui.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.okhttplearn.data.bean.Person
import io.github.okhttplearn.manager.WebSocketManager
import io.github.okhttplearn.viewmodel.DetailViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG: String = "DetailScreen"

@Composable
internal fun DetailScreen(
    person: Person,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    detailViewModel: DetailViewModel
){
    var message: String by remember {
        mutableStateOf(value = "")
    }
    // val webSocketManager: WebSocketManager = remember{
    //     WebSocketManager()
    // }
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    DisposableEffect(key1 = Unit) {
        onDispose {
            // webSocketManager.release()
        }
    }
    Column(modifier = modifier) {
        Text(text = person.toString(), modifier = Modifier.clickable {
            coroutineScope.launch {
                val result: String = detailViewModel.getHello()
                val person: Person = detailViewModel.getPerson()
                Log.i(TAG, "DetailScreen -> result: $result, person: $person")
            }
            Log.i(TAG, "DetailScreen -> sendMessage: $message")
            // webSocketManager.sendMessage(message = message)
        })
        TextField(value = message, onValueChange = {
            message = it
        })
    }
}