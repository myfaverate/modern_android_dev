package io.github.opensllearn.ui.screen

import android.os.Handler
import android.os.Looper
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.opensllearn.route.Routes

private const val TAG: String = "NavScreen"

@Composable
internal fun NavScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState
) {
    val navHostController: NavHostController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = Routes.Music
    ){
        composable<Routes.Greeting>{
            Greeting(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.Music>{
            MusicScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.Video>{
            VideoScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
    }
}