package io.github.opensllearn.utils

import android.os.Trace

private const val TAG: String = "Utils"

internal object Utils {

    init {
        System.loadLibrary("openslLearn")
        System.loadLibrary("openslLearn2")
    }

    internal fun helloTest(): String {
        return "Hello Test"
    }
    external fun getPlayer(fd: Int): Long
    external fun releasePlayer(ptr: Long)
    external fun play(ptr: Long): Boolean
    external fun pause(ptr: Long): Boolean
    external fun stop(ptr: Long): Boolean
    external fun seek(ptr: Long, duration: Int): Boolean
    external fun getDuration(ptr: Long): Int
    external fun hello1(): String
    external fun hello(fd: Int)

    external fun getRecorder(): Long
    external fun releaseRecorder(ptr: Long)

    external fun audioStart(fd: Int): Long
    external fun audioRelease(ptr: Long)
}