package edu.tyut.helloktorfit.data.remote.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import edu.tyut.helloktorfit.data.bean.Person
import edu.tyut.helloktorfit.data.bean.Result
import edu.tyut.helloktorfit.data.bean.User
import edu.tyut.helloktorfit.data.remote.service.HelloService
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import javax.inject.Singleton

private const val TAG: String = "HelloRepository"

@Singleton
internal class HelloRepository @Inject internal constructor(
    private val helloService: HelloService
) {
    internal suspend fun getHello(): String {
        return helloService.getHello()
    }
    internal suspend fun success(): Result<Boolean> {
        return helloService.success()
    }
    internal suspend fun getPerson(person: Person): Person {
        return helloService.getPerson(person = person)
    }

    internal suspend fun getUsers(): List<User> {
        return helloService.getUsers()
    }

    internal suspend fun getUser(id: Int): User {
        return helloService.getUser(id = id)
    }

    internal suspend fun download(context: Context, fileName: String, output: Uri, onProgress: (progress: Int) -> Unit = {}): Long = withContext(Dispatchers.IO) {
        val fileSize: Long? = helloService.download(fileName = fileName).execute { response: HttpResponse ->
                val totalSize: Long = response.contentLength() ?: 0L
                Log.i(TAG, "download -> totalSize: $totalSize")
                val byteReadChannel: ByteReadChannel = response.bodyAsChannel()
                context.contentResolver?.openOutputStream(output)?.use { outputStream: OutputStream ->
                    var downloadSize = 0L
                    var length = 0
                    val bytes = ByteArray(1024 * 8)
                    while (byteReadChannel.readAvailable(bytes).also { length = it } > 0){
                        downloadSize += length
                        outputStream.write(bytes, 0, length)
                        val progress: Int = (downloadSize / totalSize.toDouble() * 100).toInt()
                        onProgress(progress)
                        Log.i(TAG, "download -> progress: $progress, thread: ${Thread.currentThread()}")
                    }
                    downloadSize
                }
            }
            Log.i(TAG, "download -> fileSize: $fileSize")
            return@withContext fileSize ?: 0L
        }
}