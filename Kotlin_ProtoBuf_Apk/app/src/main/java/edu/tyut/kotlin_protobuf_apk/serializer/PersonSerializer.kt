package edu.tyut.kotlin_protobuf_apk.serializer

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import edu.tyut.kotlin_protobuf_apk.data.remote.bean.Person
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.io.InputStream
import java.io.OutputStream

// class PersonSerializer() : Serializer<Person>{
//     override val defaultValue: Person = Person(name = "unknown", age = 16, gender = "男")
// }

private const val TAG: String = "PersonSerializer"

internal class PersonSerializer() : Serializer<Person> {

    override val defaultValue: Person = Person(name = "unknown", age = 16, gender = "男")

    override suspend fun readFrom(input: InputStream): Person {
        return try {
            @OptIn(markerClass = [ExperimentalSerializationApi::class])
            ProtoBuf.decodeFromByteArray<Person>(input.readBytes())
        }catch (exception: Exception){
            Log.e(TAG, "readFrom -> message: ${exception.message}", exception)
            defaultValue
        }
    }

    override suspend fun writeTo(
        t: Person,
        output: OutputStream
    ) {
        @OptIn(markerClass = [ExperimentalSerializationApi::class])
        output.write(ProtoBuf.encodeToByteArray(t))
    }

}