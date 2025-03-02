package edu.tyut.hiltlearn.di.bean

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

internal data class Cat @AssistedInject  constructor(
    @Assisted
    internal val name: String
){
    @AssistedFactory
    internal interface Factory {
        fun create(version: String): Cat
    }
}
