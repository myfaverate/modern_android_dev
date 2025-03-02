package edu.tyut.login.data.remote.service

import edu.tyut.login.data.remote.bean.Person
import edu.tyut.login.data.remote.bean.Response
import retrofit2.http.GET

internal interface LoginService {
    @GET(value = "/login")
    suspend fun login(): Response<Person>
}