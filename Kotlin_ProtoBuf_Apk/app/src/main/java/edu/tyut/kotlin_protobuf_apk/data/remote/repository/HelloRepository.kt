package edu.tyut.kotlin_protobuf_apk.data.remote.repository

import android.app.Application
import android.util.Log
import edu.tyut.kotlin_protobuf_apk.data.remote.bean.Person
import edu.tyut.kotlin_protobuf_apk.data.remote.bean.Response
import edu.tyut.kotlin_protobuf_apk.data.remote.service.HelloService
import edu.tyut.kotlin_protobuf_apk.utils.userPreferencesStore
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val TAG: String = "HelloRepository"

internal class HelloRepository @Inject constructor(
    private val context: Application,
    private val helloService: HelloService
) {
    internal suspend fun getPerson(): Response<Person> {
        return helloService.getPerson()
    }
    /*
    context.userPreferencesStore.updateData { person: Person ->
    person.toBuilder().setUsername(userName)
        .setAge(age.toIntOrNull() ?: 0)
        .setMan(isMan)
        .build().apply {
            snackBarHostState.showSnackbar("保存成功Person: $this")
        }
    }
     */
    internal suspend fun savePerson(person: Person){
        context.userPreferencesStore.updateData { p: Person ->
            Log.i(TAG, "savePerson -> p: $p, person: $person")
            return@updateData person
        }
    }
    internal fun readPerson(): Flow<Person> {
        return context.userPreferencesStore.data.catch {
            Log.e(TAG, "readPerson -> message: ${it.message}", it)
        }
    }
}