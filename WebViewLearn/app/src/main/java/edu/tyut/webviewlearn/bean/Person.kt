package edu.tyut.webviewlearn.bean

import kotlinx.serialization.Serializable

@ConsistentCopyVisibility
@Serializable
internal data class Person internal constructor(
    internal val name: String,
    internal val age: Int,
    internal val gender: String
)