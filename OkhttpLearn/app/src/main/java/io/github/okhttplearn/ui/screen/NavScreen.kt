package io.github.okhttplearn.ui.screen

import android.util.Log
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.okhttplearn.data.bean.Person
import io.github.okhttplearn.route.Routes
import io.github.okhttplearn.viewmodel.DetailViewModel

private const val TAG: String = "NavScreen"

@Composable
internal fun NavScreen(
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState
) {
    Log.i(TAG, "NavScreen...")
    // val backStack: SnapshotStateList<Routes> = remember {
    //     mutableStateListOf<Routes>(Routes.Greeting(message = "Hello Greeting"))
    // }
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(Routes.Greeting(message = "Hello Greeting"))
    NavDisplay(
        backStack = backStack,
        onBack = {
            backStack.removeLastOrNull()
        },
        // 写法1
        // entryProvider = entryProvider {
        //     entry<Routes.Greeting>{ key: Routes.Greeting ->
        //         Greeting(message = key.message, modifier = modifier, snackBarHostState = snackBarHostState){
        //             backStack.add(element = Routes.Home)
        //         }
        //     }
        //     entry<Routes.Home> {
        //         HomeScreen(modifier = modifier, snackBarHostState = snackBarHostState)
        //     }
        // }
        // 写法2
        // entryProvider = { key: Routes ->
        //     when (key) {
        //         is Routes.Greeting -> NavEntry(key = key) {
        //             Greeting(message = key.message, modifier = modifier, snackBarHostState = snackBarHostState){
        //                 backStack.add(element = Routes.Home)
        //             }
        //         }
        //         is Routes.Home -> NavEntry(key = key) {
        //             HomeScreen(modifier = modifier, snackBarHostState = snackBarHostState){
        //                 backStack.add(element = Routes.Detail(person = Person(name = "zsh")))
        //             }
        //         }
        //         is Routes.Detail -> NavEntry<Routes>(key = key){
        //             val detailViewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        //                 creationCallback = { factory: DetailViewModel.Factory ->
        //                     factory.create(detail = key)
        //                 }
        //             )
        //             DetailScreen(person = key.person, modifier = modifier, snackBarHostState = snackBarHostState, detailViewModel = detailViewModel)
        //         }
        //     }
        // }
        entryProvider = { key ->
            when (key) {
                is Routes.Greeting -> NavEntry(key = key) {
                    Log.i(TAG, "NavScreen Greeting -> $key")
                    Greeting(message = key.message, modifier = modifier, snackBarHostState = snackBarHostState){
                        backStack.add(element = Routes.Home)
                    }
                }
                is Routes.Home -> NavEntry(key = key, contentKey = "Home") {
                    Log.i(TAG, "NavScreen Home -> $key")
                    HomeScreen(modifier = modifier, snackBarHostState = snackBarHostState){
                        backStack.add(element = Routes.Detail(person = Person(name = "zsh")))
                    }
                }
                is Routes.Detail -> NavEntry(key = key){
                    Log.i(TAG, "NavScreen Detail -> $key")
                    val detailViewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
                        creationCallback = { factory: DetailViewModel.Factory ->
                            factory.create(detail = key)
                        }
                    )
                    DetailScreen(person = key.person, modifier = modifier, snackBarHostState = snackBarHostState, detailViewModel = detailViewModel)
                }
                else -> {
                    error("Unknown route: $key")
                }
            }
        }
    )
}