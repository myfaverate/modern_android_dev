package edu.tyut.hiltlearn.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.tyut.hiltlearn.data.remote.bean.Response
import edu.tyut.hiltlearn.data.remote.repository.HelloRepository
import edu.tyut.hiltlearn.ui.state.UiState
import edu.tyut.hiltlearn.data.remote.bean.Person
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
internal class HelloViewModel @Inject constructor(
    private val helloRepository: HelloRepository
): ViewModel(){
    private val _personState: MutableStateFlow<UiState<Person>> = MutableStateFlow(UiState.Loading)
    internal val personState: StateFlow<UiState<Person>> = _personState
    internal suspend fun getPerson() {
        runCatching {
            val response: Response<Person> = helloRepository.getPerson()
            if (response.isSuccess()){
                _personState.emit(UiState.Success(response.data))
            }else{
                _personState.emit(UiState.Error(Exception(response.message)))
            }
        }.onFailure {
            _personState.emit(UiState.Error(it))
        }
    }
}