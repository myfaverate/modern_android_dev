package io.github.okhttplearn.data.remote.repository

import android.util.Log
import io.github.okhttplearn.data.bean.Person
import io.github.okhttplearn.data.remote.service.DetailService
import jakarta.inject.Inject

private const val TAG: String = "DetailRepository"

internal class DetailRepository @Inject constructor(
    private val detailService: DetailService
) {
    internal suspend fun getHello(): String {
        Log.i(TAG, "getHello -> DetailRepository: $this")
        return detailService.getHello()
    }
    internal suspend fun getPerson(): Person {
        Log.i(TAG, "getPerson -> DetailRepository: $this")
        return detailService.getPerson()
    }
}