package edu.tyut.helloktorfit.data.remote.repository

import dagger.hilt.android.scopes.ViewModelScoped
import edu.tyut.helloktorfit.data.bean.Person
import edu.tyut.helloktorfit.data.remote.service.HelloService
import jakarta.inject.Inject

@ViewModelScoped
internal class HelloRepository @Inject internal constructor(
    private val helloService: HelloService
) {
    internal suspend fun getHello(): String {
        return helloService.getHello()
    }
    internal suspend fun getPerson(person: Person): Person {
        return helloService.getPerson(person = person)
    }
}