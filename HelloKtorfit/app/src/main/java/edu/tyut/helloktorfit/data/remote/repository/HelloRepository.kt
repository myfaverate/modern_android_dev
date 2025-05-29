package edu.tyut.helloktorfit.data.remote.repository

import dagger.hilt.android.scopes.ViewModelScoped
import edu.tyut.helloktorfit.data.bean.Person
import edu.tyut.helloktorfit.data.bean.User
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

    internal suspend fun getUsers(): List<User> {
        return helloService.getUsers()
    }

    internal suspend fun getUser(id: Int): User {
        return helloService.getUser(id = id)
    }
}