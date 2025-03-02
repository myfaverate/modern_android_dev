package edu.tyut.login.di.bean

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

data class LoginUser @AssistedInject constructor(
    @Assisted
    val username: String
){
    @AssistedFactory
    interface Factory {
        fun create(username: String): LoginUser
    }
}
