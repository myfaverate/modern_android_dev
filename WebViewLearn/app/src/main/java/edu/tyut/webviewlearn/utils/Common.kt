package edu.tyut.webviewlearn.utils

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import edu.tyut.webviewlearn.bean.Person
import edu.tyut.webviewlearn.bean.Settings
import edu.tyut.webviewlearn.serializer.SettingsSerializer

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