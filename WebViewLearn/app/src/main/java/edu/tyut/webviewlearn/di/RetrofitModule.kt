package edu.tyut.webviewlearn.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.tyut.webviewlearn.BuildConfig
import edu.tyut.webviewlearn.utils.Constants
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(value = [SingletonComponent::class])
internal class RetrofitModule internal constructor(){

    // @Provides
    // @Singleton
    // internal fun providerHelloService(retrofit: Retrofit): HelloService =
    //     retrofit.create(HelloService::class.java)

    @Provides
    @Singleton
    internal fun providerOkhttpClient(/* context: Application */) : OkHttpClient {
        val okHttpClientClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
            .connectTimeout(timeout = 15, unit = TimeUnit.SECONDS)
            .readTimeout(timeout = 15, unit = TimeUnit.SECONDS)
            .writeTimeout(timeout = 15, unit = TimeUnit.SECONDS)
        if (BuildConfig.DEBUG){
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpClientClientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpClientClientBuilder.build()
    }

    @Provides
    @Singleton
    internal fun providerRetrofit(okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                Json.asConverterFactory(
                    contentType = "application/json; charset=UTF8".toMediaType()))
            .build()
    }
}