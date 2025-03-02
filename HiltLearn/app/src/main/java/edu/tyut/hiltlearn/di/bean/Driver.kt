package edu.tyut.hiltlearn.di.bean

import android.util.Log
import jakarta.inject.Inject

private const val TAG: String = "Driver"

internal class Driver @Inject constructor(){
    internal fun drive(){
        Log.i(TAG, "drive...")
    }
}