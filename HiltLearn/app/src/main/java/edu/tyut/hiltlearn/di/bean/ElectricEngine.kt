package edu.tyut.hiltlearn.di.bean

import android.util.Log
import jakarta.inject.Inject

private const val TAG: String = "ElectricEngine"

internal class ElectricEngine @Inject constructor (): Engine {
    override fun start() {
        Log.i(TAG, "Electric engine start.")
    }

    override fun shutdown() {
        Log.i(TAG, "Electric engine shutdown.")
    }
}