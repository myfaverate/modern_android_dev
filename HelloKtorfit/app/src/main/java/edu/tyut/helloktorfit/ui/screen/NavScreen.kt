package edu.tyut.helloktorfit.ui.screen

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import edu.tyut.helloktorfit.route.Routes
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

private const val TAG: String = "NavScreen"

@Composable
internal fun NavScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState
){
    val navHostController: NavHostController = rememberNavController()
    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = Routes.Greeting
    ) {
        composable<Routes.Greeting> {
            Greeting(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.Hello> { navBackStackEntry: NavBackStackEntry ->
            val hello: Routes.Hello = navBackStackEntry.toRoute<Routes.Hello>()
            Log.i(TAG, "NavScreen -> hello: $hello")
            HelloScreen(
            )
        }
    }
}