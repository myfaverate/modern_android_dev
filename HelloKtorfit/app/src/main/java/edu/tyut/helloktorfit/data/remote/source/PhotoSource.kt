package edu.tyut.helloktorfit.data.remote.source

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import edu.tyut.helloktorfit.data.bean.Photo
import edu.tyut.helloktorfit.data.remote.service.PhotoService

private const val TAG: String = "PhotoSource"

internal class PhotoSource(
    private val photoService: PhotoService
) : PagingSource<Int, Photo>() {

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition?.let { anchorPosition: Int ->
            val anchorPage: LoadResult.Page<Int, Photo>? = state.closestPageToPosition(anchorPosition = anchorPosition)
            anchorPage?.prevKey?.plus(other = 1) ?: anchorPage?.nextKey?.minus(other = 1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        return try {
            val pageIndex: Int = params.key ?: 1
            val pageSize: Int = params.loadSize
            val photos: List<Photo> = photoService.getPhotosPage(index = pageIndex, pageSize = pageSize)
            Log.i(TAG, "load -> photos: $photos")
            val prevKey: Int? = if (pageIndex > 1) pageIndex - 1 else null
            val nextKey: Int? = if (photos.isNotEmpty()) pageIndex + 1 else null
            LoadResult.Page(data = photos, prevKey = prevKey, nextKey = nextKey)
        } catch (e: Exception) {
            Log.e(TAG, "load -> message: ${e.message}", e)
            LoadResult.Error(throwable = e)
        }
    }
}