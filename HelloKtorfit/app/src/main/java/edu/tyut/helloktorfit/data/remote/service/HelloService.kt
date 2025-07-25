package edu.tyut.helloktorfit.data.remote.service

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.Streaming
import edu.tyut.helloktorfit.data.bean.Person
import edu.tyut.helloktorfit.data.bean.Result
import edu.tyut.helloktorfit.data.bean.User
import io.ktor.client.statement.HttpStatement

internal interface HelloService {
    @GET(value = "hello/hello")
    suspend fun getHello(): String
    @GET(value = "hello/success")
    suspend fun success(): Result<Boolean>
    @POST(value = "hello/person")
    suspend fun getPerson(@Body person: Person): Person
    @GET(value = "user/getUsers")
    suspend fun getUsers(): List<User>
    @GET(value = "user/getUser/{id}")
    suspend fun getUser(@Path(value = "id") id: Int): User
    @GET(value = "hello/download")
    @Streaming
    suspend fun download(@Query(value = "fileName") fileName: String): HttpStatement
    @GET(value = "{imageName}")
    @Streaming
    suspend fun getImage(@Path(value = "imageName") imageName: String): HttpStatement
}