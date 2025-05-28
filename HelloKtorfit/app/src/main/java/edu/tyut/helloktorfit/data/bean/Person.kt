package edu.tyut.helloktorfit.data.bean

import kotlinx.serialization.Serializable

@Serializable
internal data class Person(
    internal val name: String,
    internal val age: Int,
    internal val gender: String,
)
