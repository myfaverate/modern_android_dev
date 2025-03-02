package edu.tyut.hiltlearn.di.bean

import android.util.Log
import jakarta.inject.Inject

private const val TAG: String = "Truck"

internal class Truck @Inject constructor(
    private val driver: Driver,
    @BindGasEngine
    private val gasEngine: Engine,
    @BindElectricEngine
    private val electricEngine: Engine
){

    internal fun deliver() {
        driver.drive()
        gasEngine.start()
        electricEngine.start()
        Log.i(TAG, "deliver Truck is delivering cargo. Driven by $driver")
        gasEngine.shutdown()
        electricEngine.shutdown()
    }
}