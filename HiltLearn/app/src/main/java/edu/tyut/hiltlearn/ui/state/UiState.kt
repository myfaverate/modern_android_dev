package edu.tyut.hiltlearn.ui.state

internal sealed class UiState<out T> {
    internal data object IDLE : UiState<Nothing>()
    internal data object Loading : UiState<Nothing>()
    internal data class Success<T>(val data: T) : UiState<T>()
    internal data class Error(val exception: Throwable) : UiState<Nothing>()
}