package edu.tyut.helloktorfit.data.remote.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import edu.tyut.helloktorfit.data.bean.Person

internal interface HelloService {
    @GET(value = "hello/hello")
    suspend fun getHello(): String
    @POST(value = "hello/person")
    suspend fun getPerson(@Body person: Person): Person
}