package edu.tyut.helloktorfit.data.bean

import kotlinx.serialization.Serializable

@Serializable
internal data class User (
    internal val id: Int,
    internal val account: String,
    internal val password: String,
    internal val nickname: String,
)