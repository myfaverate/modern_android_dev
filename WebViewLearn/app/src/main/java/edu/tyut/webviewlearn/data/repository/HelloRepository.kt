package edu.tyut.webviewlearn.data.repository

import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import edu.tyut.webviewlearn.bean.Person
import edu.tyut.webviewlearn.data.local.dao.PersonDao
import edu.tyut.webviewlearn.data.remote.service.HelloService
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okio.source

private const val TAG: String = "HelloRepository"

@ViewModelScoped
internal class HelloRepository @Inject internal constructor(
    private val helloService: HelloService,
    private val personDao: PersonDao,
){
    internal suspend fun insert(person: Person): Long {
        return personDao.insert(person = person)
    }
    internal suspend fun hello(): Flow<String>  = withContext(Dispatchers.IO){
        val persons: Flow<Person> = personDao.getPersons().asFlow()
        helloService.hello().charStream().forEachLine {
            Log.i(TAG, "hello -> content: $it")
        }
        val hellos: Flow<String> = flow {
            emit("sss")
        }
            .flowOn(Dispatchers.IO)
        persons.map {
            Log.i(TAG, "hello -> person: $it")
            it.toString()
        }.combine(flow = hellos) { person: String, hello: String ->
            "person: $person, hello: $hello"
        }
    }
}