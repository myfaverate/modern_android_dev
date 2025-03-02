package edu.tyut.hiltlearn.data.remote.repository

import edu.tyut.hiltlearn.data.remote.bean.Response
import edu.tyut.hiltlearn.data.remote.service.HelloService
import edu.tyut.hiltlearn.data.remote.bean.Person
import jakarta.inject.Inject

internal class HelloRepository @Inject constructor(
    private val helloService: HelloService
) {
    internal suspend fun getPerson(): Response<Person> {
        return helloService.getPerson()
    }
}