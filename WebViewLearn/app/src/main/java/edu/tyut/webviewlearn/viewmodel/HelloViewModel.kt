package edu.tyut.webviewlearn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.tyut.webviewlearn.bean.Person
import edu.tyut.webviewlearn.data.repository.HelloRepository
import kotlinx.coroutines.flow.Flow

private const val TAG: String = "HelloViewModel"

@HiltViewModel(assistedFactory = HelloViewModel.HelloViewModelFactory::class)
internal class HelloViewModel @AssistedInject internal constructor(
    private val helloRepository: HelloRepository,
    @Assisted private val name: String,
): ViewModel() {
    internal suspend fun insert(person: Person): Long {
        Log.i(TAG, "insert -> name: $name")
        return helloRepository.insert(person = person.copy(name = name))
    }
    internal suspend fun hello(): Flow<String> {
        return helloRepository.hello()
    }
    @AssistedFactory
    internal interface HelloViewModelFactory {
        fun create(name: String): HelloViewModel
    }
}