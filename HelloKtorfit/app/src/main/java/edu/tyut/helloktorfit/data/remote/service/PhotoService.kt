package edu.tyut.helloktorfit.data.remote.service

import androidx.compose.foundation.pager.PageSize
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.Multipart
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Part
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import edu.tyut.helloktorfit.data.bean.Photo
import edu.tyut.helloktorfit.data.bean.Result
import edu.tyut.helloktorfit.data.bean.User
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.content.PartData

internal interface PhotoService {

    @GET(value = "photo/getPhotos")
    suspend fun getPhotos(): List<Photo>

    @GET(value = "photo/getPhotosPage")
    suspend fun getPhotosPage(
        @Query(value = "pageIndex") index: Int,
        @Query(value = "pageSize") pageSize: Int
    ): List<Photo>

    /**
     * 有 bug
     */
    @Multipart
    @POST(value = "photo/insert")
    suspend fun insert(
        @Part(value = "photoName")
        photoName: String,
        @Part(value = "description")
        description: String,
        @Part(value = "photo")
        photo: List<PartData>
    ): Result<Boolean>

    @Multipart
    @POST(value = "photo/insert")
    suspend fun insert1(
        @Body multiPart: MultiPartFormDataContent
    ): Result<Boolean>

    @Multipart
    @PUT(value = "photo/update")
    suspend fun update(
        @Body multiPart: MultiPartFormDataContent
    ): Result<Boolean>

    @DELETE(value = "photo/delete")
    suspend fun deleteById(
        @Body photo: Photo
    ): Result<Boolean>

    @GET(value = "user/getUsers")
    suspend fun getUsers(): List<User>
    @GET(value = "user/getUser/{id}")
    suspend fun getUser(@Path(value = "id") id: Int): User
}