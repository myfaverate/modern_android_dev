package edu.tyut.helloktorfit.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.ktorfit
import edu.tyut.helloktorfit.data.remote.service.HelloService
import edu.tyut.helloktorfit.data.remote.service.createHelloService
import edu.tyut.helloktorfit.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton


private const val TAG: String = "KtorFitModule"

@Module
@InstallIn(value = [SingletonComponent::class])
internal class KtorFitModule {

    @Provides
    @Singleton
    internal fun providerHelloService(ktorfit: Ktorfit): HelloService =
        ktorfit.createHelloService()

    @Provides
    @Singleton
    internal fun providerHttpClient(): HttpClient {
        return HttpClient(engineFactory = OkHttp){
            engine {
                // addInterceptor {  }
                // addNetworkInterceptor {  }
            }
            install(plugin = ContentNegotiation) {
                json(json = Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            install(plugin = HttpTimeout) {
                connectTimeoutMillis = 3000L
                requestTimeoutMillis = 3000L
                socketTimeoutMillis = 3000L
            }
            install(plugin = DefaultRequest) {
                header(key = HttpHeaders.ContentType, value = ContentType.Application.Json)
                header(key = HttpHeaders.Accept, value = ContentType.Application.Json)
            }
            install(plugin = Logging){
                logger = Logger.ANDROID
                level = LogLevel.ALL
            }
        }
    }

    @Provides
    @Singleton
    internal fun providerKtorFit(httpClient: HttpClient): Ktorfit {
        Log.i(TAG, "Engine: ${httpClient.engine::class.qualifiedName}")
        // return Ktorfit.Builder()
        //     .httpClient(httpClient)
        //     .baseUrl(Constants.BASE_URL)
        //     .build()
        return ktorfit {
            httpClient(client = httpClient)
            baseUrl(url = Constants.BASE_URL)
        }
    }
}