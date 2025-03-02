package edu.tyut.hiltlearn.di.bean

import android.util.Log
import jakarta.inject.Inject

private const val TAG: String = "GasEngine"

internal class GasEngine @Inject constructor () : Engine {
    override fun start() {
        Log.i(TAG, "Gas engine start.")
    }

    override fun shutdown() {
        Log.i(TAG, "Gas engine shutdown.")
    }
}