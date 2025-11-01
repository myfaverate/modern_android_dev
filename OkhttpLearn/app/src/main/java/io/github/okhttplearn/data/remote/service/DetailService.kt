package io.github.okhttplearn.data.remote.service

import io.github.okhttplearn.data.bean.Person
import retrofit2.http.GET

internal interface DetailService {
    @GET(value = "/hello/hello")
    suspend fun getHello(): String

    @GET(value = "/hello/person")
    suspend fun getPerson(): Person
}