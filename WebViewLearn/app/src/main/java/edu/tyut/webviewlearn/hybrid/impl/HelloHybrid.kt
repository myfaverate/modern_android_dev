package edu.tyut.webviewlearn.hybrid.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.navigation.NavHostController
import edu.tyut.webviewlearn.bean.Person
import edu.tyut.webviewlearn.hybrid.Hybrid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private const val TAG: String = "HelloHybrid"

/**
 * const HYBRID: String = "hybrid"
 * let result: String | null = prompt(`${Constants.HYBRID}://startShopPage?page=shop&shop=${JSON.stringify(shop)}`)
 * document.writeln(`startShopPage -> result: ${result}<br/>`)
 */
internal class HelloHybrid internal constructor() : Hybrid {
    override fun getName(): String {
        return "helloWorld"
    }

    override suspend fun onAction(
        context: Context, webView: WebView, navHostController: NavHostController, uri: Uri,
        snackBarHostState: SnackbarHostState,
    ): String {
        snackBarHostState.showSnackbar("Hello SnackBar! uri: $uri")
        delay(timeMillis = 100L)
        Toast.makeText(context, uri.toString(), Toast.LENGTH_SHORT).show()
        val personJson: String = uri.getQueryParameter("params") ?: ""
        val person: Person = Json.decodeFromString<Person>(personJson)
        Log.i(TAG, "onAction: person: $person, personJson: $personJson")
        return Json.encodeToString(value = person.copy(name = "android: android"))
    }

}