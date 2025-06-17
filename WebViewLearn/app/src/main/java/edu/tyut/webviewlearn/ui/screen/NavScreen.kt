package edu.tyut.webviewlearn.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import edu.tyut.webviewlearn.route.Routes

private const val TAG: String = "NavScreen"

@Composable
internal fun NavScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState
){
    val navHostController: NavHostController = rememberNavController()
    NavHost(
        modifier = Modifier,
        navController = navHostController,
        startDestination = Routes.Greeting
    ) {
        composable<Routes.Greeting>{
            Greeting(
                modifier = modifier,
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.Hello>{
            HelloScreen(modifier = modifier)
        }
        composable<Routes.WebView> { navBackStackEntry: NavBackStackEntry ->
            val webView: Routes.WebView = navBackStackEntry.toRoute<Routes.WebView>()
            Log.i(TAG, "NavScreen -> webView: $webView")
            WebViewScreen(
                navHostController = navHostController,
                url = webView.url
            )
        }
    }
}