package edu.tyut.helloktorfit.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.tyut.helloktorfit.data.bean.Person
import edu.tyut.helloktorfit.data.remote.repository.HelloRepository
import jakarta.inject.Inject

@HiltViewModel
internal class HelloViewModel @Inject internal constructor(
    private val helloRepository: HelloRepository
): ViewModel() {
    internal suspend fun getHello(): String {
        return helloRepository.getHello()
    }
    internal suspend fun getPerson(person: Person): Person {
        return helloRepository.getPerson(person = person)
    }
}