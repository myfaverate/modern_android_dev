package io.github.okhttplearn.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.okhttplearn.data.bean.Person
import io.github.okhttplearn.data.remote.repository.DetailRepository
import io.github.okhttplearn.route.Routes

private const val TAG: String = "GreetingViewModel"

@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
internal class DetailViewModel @AssistedInject internal constructor(
    @Assisted
    private val detail: Routes.Detail,
    private val helloRepository: DetailRepository,
) : ViewModel(){
    @AssistedFactory
    internal interface Factory {
        fun create(detail: Routes.Detail) : DetailViewModel
    }
    internal suspend fun getHello(): String {
        Log.i(TAG, "getHello -> detail: $detail")
        return helloRepository.getHello()
    }
    internal suspend fun getPerson(): Person {
        Log.i(TAG, "getPerson -> DetailRepository: $this")
        return helloRepository.getPerson()
    }
}