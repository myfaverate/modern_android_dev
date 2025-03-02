package edu.tyut.kotlin_protobuf_apk.data.remote.service

import edu.tyut.kotlin_protobuf_apk.data.remote.bean.Person
import edu.tyut.kotlin_protobuf_apk.data.remote.bean.Response
import retrofit2.http.GET

internal interface HelloService {
    @GET(value = "/hello")
    suspend fun getPerson(): Response<Person>
}