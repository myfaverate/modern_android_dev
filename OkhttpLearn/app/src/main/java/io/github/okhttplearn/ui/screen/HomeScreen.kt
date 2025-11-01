package io.github.okhttplearn.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

private const val TAG: String = "HomeScreen"

@Composable
internal fun HomeScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState,
    onNavigationToDetail: () -> Unit,
){
    Text(text = TAG, modifier = modifier.clickable(onClick = onNavigationToDetail))
}