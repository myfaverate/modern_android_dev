package edu.tyut.helloktorfit.ui.screen

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import edu.tyut.helloktorfit.data.bean.Photo
import edu.tyut.helloktorfit.route.Routes
import edu.tyut.helloktorfit.route.photoNavType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

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
        startDestination = Routes.HelloCrop
    ) {
        composable<Routes.Greeting> {
            Greeting(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.PhotoScreen>(
            typeMap = mapOf<KType, NavType<Photo>>(
                typeOf<Photo>() to photoNavType
            )
        ) { navBackStackEntry: NavBackStackEntry ->
            val photoScreen: Routes.PhotoScreen = navBackStackEntry.toRoute<Routes.PhotoScreen>()
            Log.i(TAG, "NavScreen -> photoScreen: $photoScreen")
            PictureScreen(
                photo = photoScreen.photo,
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.Hello> { navBackStackEntry: NavBackStackEntry ->
            val hello: Routes.Hello = navBackStackEntry.toRoute<Routes.Hello>()
            Log.i(TAG, "NavScreen -> hello: $hello")
            HelloScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.Service> { _: NavBackStackEntry ->
            ServiceScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.Provider> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Provider...")
            ProviderScreen(
                modifier = Modifier,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.Crop> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Crop...")
            CropScreen(
                modifier = Modifier,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.Image> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Image...")
            ImageScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }

        composable<Routes.Image1> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Image...")
            ImageScreen1(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }

        composable<Routes.Gif> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Gif...")
            GifScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }

        composable<Routes.Audio> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Audio...")
            AudioScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }

        composable<Routes.Binder> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Binder...")
            BinderScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }

        composable<Routes.System> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Binder...")
            SystemScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
        composable<Routes.MediaCodec> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Binder...")
            MediaCodecScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }

        composable<Routes.HelloCrop> { navBackStackEntry: NavBackStackEntry ->
            Log.i(TAG, "NavScreen -> Binder...")
            HelloCropScreen(
                navHostController = navHostController,
                snackBarHostState = snackBarHostState
            )
        }
    }
}