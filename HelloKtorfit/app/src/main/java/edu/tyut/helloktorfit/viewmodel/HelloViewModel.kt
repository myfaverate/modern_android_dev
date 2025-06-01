package edu.tyut.helloktorfit.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.tyut.helloktorfit.data.bean.Person
import edu.tyut.helloktorfit.data.bean.Result
import edu.tyut.helloktorfit.data.bean.User
import edu.tyut.helloktorfit.data.remote.repository.HelloRepository
import jakarta.inject.Inject

@HiltViewModel
internal class HelloViewModel @Inject internal constructor(
    private val helloRepository: HelloRepository
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
}