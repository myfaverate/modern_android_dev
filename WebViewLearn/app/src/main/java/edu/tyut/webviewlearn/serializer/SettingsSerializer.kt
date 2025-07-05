package edu.tyut.webviewlearn.serializer

import android.util.Log
import androidx.datastore.core.Serializer
import edu.tyut.webviewlearn.bean.Person
import edu.tyut.webviewlearn.bean.Settings
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

private const val TAG: String = "SettingsSerializer"

@OptIn(markerClass = [ExperimentalSerializationApi::class])
internal class SettingsSerializer internal constructor(
    override val defaultValue: Settings = Settings(
        person = Person(id = 0, name = "zsh", age = 0, gender = "男"),
    )
) : Serializer<Settings> {
    override suspend fun readFrom(input: InputStream): Settings {
        return try {
            ProtoBuf.decodeFromByteArray<Settings>(bytes = input.readBytes())
        }catch (e: Exception){
            Log.e(TAG, "readFrom message: ${e.message}", e)
            defaultValue
        }
    }

    override suspend fun writeTo(
        t: Settings,
        output: OutputStream
    ) {
        output.write(ProtoBuf.encodeToByteArray(value = t))
    }

}