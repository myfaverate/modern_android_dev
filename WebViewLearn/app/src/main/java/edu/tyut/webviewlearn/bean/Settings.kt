package edu.tyut.webviewlearn.bean

import kotlinx.serialization.Serializable

@Serializable
@ConsistentCopyVisibility
internal data class Settings internal constructor(
    internal val person: Person
)
