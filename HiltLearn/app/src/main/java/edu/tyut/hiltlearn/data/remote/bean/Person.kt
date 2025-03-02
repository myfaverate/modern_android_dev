package edu.tyut.hiltlearn.data.remote.bean

import kotlinx.serialization.Serializable

@Serializable
@OptIn(markerClass = [kotlinx.serialization.InternalSerializationApi::class])
internal data class Person(
    internal val name: String,
    internal val age: Int,
    internal val gender: String,
)
