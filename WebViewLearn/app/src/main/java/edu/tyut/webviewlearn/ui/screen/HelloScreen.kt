package edu.tyut.webviewlearn.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val TAG: String = "HelloScreen"

@Composable
internal fun HelloScreen(
    modifier: Modifier
){
    Column(
        modifier = Modifier.statusBarsPadding()
    ){
        Text(text = "Hello")
    }
}