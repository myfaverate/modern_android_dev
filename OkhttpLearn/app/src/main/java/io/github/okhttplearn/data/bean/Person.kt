package io.github.okhttplearn.data.bean

import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class Person internal constructor(
    internal val name: String,
    internal val description: String
)
