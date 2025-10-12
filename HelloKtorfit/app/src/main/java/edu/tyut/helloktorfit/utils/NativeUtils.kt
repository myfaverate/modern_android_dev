package edu.tyut.helloktorfit.utils

internal object NativeUtils {
    init {
        System.loadLibrary("helloKtorFit1")
    }
    external fun helloWorld()
}