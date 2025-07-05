package edu.tyut.webviewlearn.utils

internal object NativeUtils {
    init {
        System.loadLibrary("webViewLearn")
    }
    external fun stringFromJNI(): String
}