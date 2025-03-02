package edu.tyut.kotlin_protobuf_apk.data.remote.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@OptIn(markerClass = [kotlinx.serialization.InternalSerializationApi::class])
internal data class Response<T>(
    internal val code: Int,
    @SerialName(value = "message")
    internal val message: String,
    internal val data: T
){
    internal fun isSuccess(): Boolean {
        return code == 200
    }
}
