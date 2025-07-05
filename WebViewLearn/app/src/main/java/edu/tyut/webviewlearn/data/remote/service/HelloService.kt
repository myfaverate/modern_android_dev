package edu.tyut.webviewlearn.data.remote.service

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

internal interface HelloService {
    @GET(value = "hello")
    @Streaming
    suspend fun hello(): ResponseBody
}