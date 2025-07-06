package edu.tyut.webviewlearn.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "person")
@ConsistentCopyVisibility
@Serializable
internal data class Person internal constructor(
    @PrimaryKey(autoGenerate = true)
    internal val id: Long,
    internal val name: String,
    internal val age: Int,
    internal val gender: String
)