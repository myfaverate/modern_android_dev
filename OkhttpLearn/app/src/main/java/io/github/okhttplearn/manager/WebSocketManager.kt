package io.github.okhttplearn.manager

import android.util.Log
import io.github.okhttplearn.utils.Constants
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.time.Duration

private const val TAG: String = "WebSocketManager"

internal class WebSocketManager internal constructor(){

    private val client: OkHttpClient = OkHttpClient.Builder()
        .readTimeout(duration = Duration.ofMillis(0L))
        .build()

    private val request: Request = Request.Builder()
        .url(Constants.BASE_WS_URL)
        .build()

    private val listener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.i(TAG, "onOpen...")
            webSocket.send("Open Hello Client Android")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.i(TAG, "onMessage -> receive: $text")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "onClose -> code: $code, reason: $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.i(TAG, "onFailure -> error: ", t)
        }

    }

    private val webSocket: WebSocket = client.newWebSocket(request = request, listener = listener)

    internal fun sendMessage(message: String){
        val isSuccess: Boolean = webSocket.send(text = message)
        Log.i(TAG, "sendMessage -> isSuccess: $isSuccess")
    }

    internal fun release(){
        webSocket.close(code = 1000, reason = "close")
    }
}