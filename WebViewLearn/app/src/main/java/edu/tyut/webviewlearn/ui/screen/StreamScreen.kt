package edu.tyut.webviewlearn.ui.screen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.tyut.webviewlearn.bean.Person
import edu.tyut.webviewlearn.bean.Settings
import edu.tyut.webviewlearn.ui.theme.RoundedCornerShape10
import edu.tyut.webviewlearn.utils.dataStore
import edu.tyut.webviewlearn.utils.settingsDataStore
import edu.tyut.webviewlearn.viewmodel.HelloViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val TAG: String = "StreamScreen"

@Composable
internal fun StreamScreen(
    modifier: Modifier,
    snackBarHostState: SnackbarHostState,
    helloViewModel: HelloViewModel = hiltViewModel(
        creationCallback = { factory: HelloViewModel.HelloViewModelFactory ->
            factory.create(name = "Screen Zsh")
        }
    )
){
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val countKey: Preferences.Key<Int> = intPreferencesKey(name = "count")
    val count: Int by context.dataStore.data.map { preferences: Preferences ->
        preferences[countKey] ?: 0
    }.collectAsStateWithLifecycle(initialValue = 0)
    val settings: Settings by context.settingsDataStore.data.collectAsStateWithLifecycle(initialValue = Settings(person = Person(id= 0, name = "z", age = 0, gender = "男")))
    Column(modifier = modifier.fillMaxSize()){
        Text(
            text = "插入Person: count: $count，settings: $settings",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    coroutineScope.launch {
                        val person = Person(
                            id = 0,
                            name = "张书豪",
                            age = 18,
                            gender = "男"
                        )
                        val rows: Long = helloViewModel.insert(
                            person = person
                        )
                        val countKey: Preferences.Key<Int> = intPreferencesKey(name = "count")
                        val preferences: Preferences =
                            context.dataStore.edit { mutablePreferences: MutablePreferences ->
                                mutablePreferences[countKey] = Random.nextInt(0, 100000)
                            }
                        val count: Int? = preferences.get(key = countKey)
                        Log.i(TAG, "StreamScreen -> count: $count, rows: $rows")
                        context.settingsDataStore.updateData { settings: Settings ->
                            Log.i(TAG, "StreamScreen -> prev Settings: $settings")
                            settings.copy(person = Person(id = 1, name = "zdddidx: ${Random.nextInt(10000)}", age = 18, gender = "男"))
                        }
                    }
                },
            color = Color.White
        )
        Text(
            text = "获取数据",
            Modifier
                .padding(top = 10.dp)
                .background(color = Color.Black, shape = RoundedCornerShape10)
                .padding(all = 5.dp)
                .clickable {
                    coroutineScope.launch {
                        helloViewModel.hello().collect {
                            Log.i(TAG, "StreamScreen -> hello: $it")
                        }
                    }
                },
            color = Color.White
        )
    }
}