package edu.tyut.helloktorfit.ui.screen

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.placeholder
import edu.tyut.helloktorfit.R
import edu.tyut.helloktorfit.utils.NativeUtils
import edu.tyut.helloktorfit.viewmodel.HelloViewModel

@Composable
internal fun JNIScreen(
    navHostController: NavHostController,
    snackBarHostState: SnackbarHostState,
) {
    val context: Context = LocalContext.current
    Text(text = "Hello World", modifier = Modifier.clickable {
        NativeUtils.helloWorld()
    })
}