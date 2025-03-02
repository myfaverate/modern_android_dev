package edu.tyut.kotlin_protobuf_apk.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import edu.tyut.kotlin_protobuf_apk.data.remote.bean.Person
import edu.tyut.kotlin_protobuf_apk.serializer.PersonSerializer


private const val PREFERENCE_NAME = "setting"
// 1
// internal var dataStore: DataStore<Preferences> = context.createDataStore(name = PREFERENCE_NAME)
// 2
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)

internal val Context.userPreferencesStore: DataStore<Person> by dataStore(
    fileName = "person_prefs.pb",
    serializer = PersonSerializer()
)

