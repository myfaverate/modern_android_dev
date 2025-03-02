package edu.tyut.login.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.tyut.login.data.remote.bean.Person
import edu.tyut.login.data.remote.bean.Response
import edu.tyut.login.data.remote.repository.LoginRepository
import edu.tyut.login.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
internal class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
): ViewModel(){
    private val _personState: MutableStateFlow<UiState<Person>> = MutableStateFlow(UiState.Loading)
    internal val personState: StateFlow<UiState<Person>> = _personState
    internal suspend fun login() {
        runCatching {
            val response: Response<Person> = loginRepository.login()
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