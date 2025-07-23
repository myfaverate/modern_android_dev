package edu.tyut.helloktorfit.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.tyut.helloktorfit.data.bean.Person
import edu.tyut.helloktorfit.data.bean.Result
import edu.tyut.helloktorfit.data.bean.User
import edu.tyut.helloktorfit.data.remote.repository.HelloRepository
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.use
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.streams.asByteWriteChannel
import jakarta.inject.Inject
import java.io.OutputStream

private const val TAG: String = "HelloViewModel"

@HiltViewModel
internal class HelloViewModel @Inject internal constructor(
    private val helloRepository: HelloRepository,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    internal suspend fun getHello(): String {
        return helloRepository.getHello()
    }
    internal suspend fun success(): Result<Boolean> {
        return try {
            helloRepository.success()
        }catch (e: Exception){
            Result.failure(message = e.message ?: "", data = false)
        }
    }
    internal suspend fun getPerson(person: Person): Person {
        return helloRepository.getPerson(person = person)
    }
    internal suspend fun getUsers(): List<User> {
        return helloRepository.getUsers()
    }
    internal suspend fun getUser(id: Int): User {
        return helloRepository.getUser(id = id)
    }
    internal suspend fun download(context: Context, fileName: String, output: Uri): Long {
        return helloRepository.download(context = context, fileName = fileName, output = output)
    }
    internal suspend fun getImage(imageName: String): HttpStatement {
        return helloRepository.getImage(imageName = imageName)
    }
}