package io.github.okhttplearn.di

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.okhttplearn.BuildConfig
import io.github.okhttplearn.data.remote.service.DetailService
import io.github.okhttplearn.utils.Constants
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(value = [SingletonComponent::class])
internal class RetrofitModule internal constructor(){

    @Provides
    @Singleton
    internal fun providerDetailService(retrofit: Retrofit): DetailService =
        retrofit.create(DetailService::class.java)

    @Provides
    @Singleton
    internal fun providerOkhttpClient(/* context: Application */) : OkHttpClient {
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


    @Provides
    @Singleton
    internal fun providerRetrofit(okHttpClient: OkHttpClient) : Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                object : Converter.Factory() {
                    override fun responseBodyConverter(
                        type: Type,
                        annotations: Array<out Annotation?>,
                        retrofit: Retrofit
                    ): Converter<ResponseBody, *>? {
                        return if (type == String::class.java){
                            Converter<ResponseBody, String> { responseBody: ResponseBody ->
                                responseBody.string()
                            }
                        } else {
                            null
                        }
                    }
                }
            )
            .addConverterFactory(
                Json.asConverterFactory(
                    contentType = "application/json; charset=UTF8".toMediaType()))
            .build()
    }
}