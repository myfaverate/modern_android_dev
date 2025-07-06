package edu.tyut.webviewlearn.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import edu.tyut.webviewlearn.bean.Person
import edu.tyut.webviewlearn.bean.Settings
import edu.tyut.webviewlearn.serializer.SettingsSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

private const val TAG: String = "Common"

internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dataStore")
internal val Context.settingsDataStore: DataStore<Settings> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsSerializer(),
    corruptionHandler = ReplaceFileCorruptionHandler { throwable: Throwable ->
        Log.e(TAG, "文件损坏数据丢失...", throwable)
        Settings(person = Person(id = 0, name = "zsh", age = 0, gender = "男"))
    }
)

/**
 * 只能使用于接收系统广播，不建议瞎jb使用广播和类似的EventBus这种框架
 */
internal fun Context.broadcastAsFlow(vararg actions: String): Flow<Intent> = callbackFlow<Intent> {
    val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null){
                val result: ChannelResult<Unit> = this@callbackFlow.trySendBlocking(element = intent)
                Log.i(TAG, "onReceive -> result: $result")
            }
        }
    }
    val filter: IntentFilter = IntentFilter().apply {
        actions.forEach {  action ->
            addAction(action)
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    } else {
        registerReceiver(receiver, filter)
    }
    awaitClose {
        Log.i(TAG, "broadcastAsFlow awaitClose ...")
        unregisterReceiver(receiver)
    }
}.buffer(capacity = Channel.UNLIMITED)