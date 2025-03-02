package edu.tyut.kotlin_protobuf_apk.data.remote.bean

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@OptIn(markerClass = [InternalSerializationApi::class])
internal data class Person(
    internal val name: String,
    internal val age: Int,
    internal val gender: String,
)
