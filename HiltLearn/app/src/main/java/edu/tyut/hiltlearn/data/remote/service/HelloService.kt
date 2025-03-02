package edu.tyut.hiltlearn.data.remote.service

import edu.tyut.hiltlearn.data.remote.bean.Response
import edu.tyut.hiltlearn.data.remote.bean.Person
import retrofit2.http.GET

internal interface HelloService {
    @GET(value = "/getPerson")
    suspend fun getPerson(): Response<Person>
}