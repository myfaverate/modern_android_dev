package edu.tyut.kotlin_protobuf.bean

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val name: String,
    val age: Int,
    val gender: String,
)