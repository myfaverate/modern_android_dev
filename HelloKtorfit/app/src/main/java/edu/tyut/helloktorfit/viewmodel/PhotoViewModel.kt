package edu.tyut.helloktorfit.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.tyut.helloktorfit.data.bean.Person
import edu.tyut.helloktorfit.data.bean.Photo
import edu.tyut.helloktorfit.data.bean.Result
import edu.tyut.helloktorfit.data.bean.User
import edu.tyut.helloktorfit.data.remote.repository.HelloRepository
import edu.tyut.helloktorfit.data.remote.repository.PhotoRepository
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.content.PartData
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG: String = "PhotoViewModel"

@HiltViewModel
internal class PhotoViewModel @Inject internal constructor(
    private val photoRepository: PhotoRepository
): ViewModel() {
    private val _photosFlow: MutableStateFlow<List<Photo>> = MutableStateFlow<List<Photo>>(value = emptyList<Photo>())
    internal val photosFlow: StateFlow<List<Photo>> = _photosFlow
    internal suspend fun refreshPhotos(id: Long){
        _photosFlow.emit(value = _photosFlow.value.filter { it.id != id })
    }
    internal suspend fun getPhotos() {
        runCatching {
            val photos: List<Photo> = photoRepository.getPhotos()
            _photosFlow.emit(value = photos)
        }.onFailure {
            Log.e(TAG, "getPhotos -> error: ${it.message}", it)
        }
    }
    internal suspend fun insert(
        photoName: String,
        description: String,
        photo: List<PartData>,
    ): Boolean {
        return try {
            val result: Result<Boolean> = photoRepository.insert(
                photoName = photoName,
                description = description,
                photo = photo
            )
            Log.i(TAG, "insert -> result: $result")
            result.data
        } catch (e: Exception) {
            Log.e(TAG, "insert -> error: ${e.message}", e)
            false
        }
    }

    internal suspend fun insert1(
        multiPart: MultiPartFormDataContent
    ): Boolean {
        return try {
            val result: Result<Boolean> = photoRepository.insert1(
                multiPart = multiPart
            )
            Log.i(TAG, "insert -> result: $result")
            result.data
        } catch (e: Exception) {
            Log.e(TAG, "insert -> error: ${e.message}", e)
            false
        }
    }

    internal suspend fun update(
        multiPart: MultiPartFormDataContent
    ): Boolean {
        return try {
            val result: Result<Boolean> = photoRepository.update(
                multiPart = multiPart
            )
            Log.i(TAG, "update -> result: $result")
            result.data
        } catch (e: Exception) {
            Log.e(TAG, "update -> error: ${e.message}", e)
            false
        }
    }

    internal suspend fun deleteById(
        photo: Photo
    ): Boolean {
        return try {
            val result: Result<Boolean> = photoRepository.deleteById(
                photo = photo
            )
            Log.i(TAG, "deleteById -> result: $result")
            result.data
        } catch (e: Exception) {
            Log.e(TAG, "deleteById -> error: ${e.message}", e)
            false
        }
    }

    // 查询生成结果列表
    internal val photosPageFlow:  Flow<PagingData<Photo>> = photoRepository.getPhotosPage(pageSize = 20).cachedIn(scope = viewModelScope)

}