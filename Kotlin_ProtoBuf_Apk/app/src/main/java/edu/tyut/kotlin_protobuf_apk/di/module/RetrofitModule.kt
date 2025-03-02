package edu.tyut.kotlin_protobuf_apk.di.module

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.tyut.hiltlearn.utils.Constants
import edu.tyut.kotlin_protobuf_apk.BuildConfig
import edu.tyut.kotlin_protobuf_apk.data.remote.service.HelloService
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
internal class RetrofitModule {

    @Provides
    @Singleton
    internal fun providerHelloService(retrofit: Retrofit): HelloService =
        retrofit.create(HelloService::class.java)

    @Provides
    @Singleton
    internal fun providerOkhttpClient(context: Application) : OkHttpClient {
        val okHttpClientClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG){
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            okHttpClientClientBuilder.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpClientClientBuilder.build()
    }

    @OptIn(markerClass = [ExperimentalSerializationApi::class])
    @Provides
    @Singleton
    internal fun providerRetrofit(okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ProtoBuf.asConverterFactory(
                "application/x-protobuf".toMediaType()))
            .build()
    }
}