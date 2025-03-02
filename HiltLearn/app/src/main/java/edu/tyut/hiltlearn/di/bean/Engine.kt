package edu.tyut.hiltlearn.di.bean

import jakarta.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class BindGasEngine

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class BindElectricEngine


internal interface Engine {
    fun start()
    fun shutdown()
}