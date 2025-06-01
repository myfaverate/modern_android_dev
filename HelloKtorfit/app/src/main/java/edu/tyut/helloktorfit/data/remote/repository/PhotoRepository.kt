package edu.tyut.helloktorfit.data.remote.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.hilt.android.scopes.ViewModelScoped
import edu.tyut.helloktorfit.data.bean.Photo
import edu.tyut.helloktorfit.data.bean.Result
import edu.tyut.helloktorfit.data.remote.service.PhotoService
import edu.tyut.helloktorfit.data.remote.source.PhotoSource
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.content.PartData
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.toList

private const val TAG: String = "PhotoRepository"

@ViewModelScoped
internal class PhotoRepository @Inject internal constructor(
    private val photoService: PhotoService
) {
    internal suspend fun getPhotos(): List<Photo> {
        return photoService.getPhotos()
    }

    internal suspend fun insert(
        photoName: String,
        description: String,
        photo: List<PartData>,
    ): Result<Boolean> {
        return photoService.insert(
            photoName = photoName,
            description = description,
            photo = photo
        )
    }

    internal suspend fun insert1(
        multiPart: MultiPartFormDataContent
    ): Result<Boolean> {
        return photoService.insert1(
            multiPart = multiPart
        )
    }

    internal suspend fun update(
        multiPart: MultiPartFormDataContent
    ): Result<Boolean> {
        return photoService.update(
            multiPart = multiPart
        )
    }

    internal suspend fun deleteById(
        photo: Photo
    ): Result<Boolean> {
        return photoService.deleteById(
            photo = photo
        )
    }

    internal fun getPhotosPage(pageSize: Int): Flow<PagingData<Photo>> {
        val pager: Pager<Int, Photo> = Pager(
            config = PagingConfig(pageSize = pageSize, initialLoadSize = pageSize * 3),
            pagingSourceFactory = {
                return@Pager PhotoSource(photoService = photoService)
            }
        )
        return pager.flow
    }
}