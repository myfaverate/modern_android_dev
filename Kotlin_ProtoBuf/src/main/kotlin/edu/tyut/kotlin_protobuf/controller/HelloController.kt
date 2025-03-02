package edu.tyut.kotlin_protobuf.controller

import edu.tyut.kotlin_protobuf.bean.Person
import edu.tyut.kotlin_protobuf.bean.Result
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
internal class HelloController {
    @OptIn(markerClass = [ExperimentalSerializationApi::class])
    @GetMapping(value = ["/hello"], produces = [MediaType.APPLICATION_PROTOBUF_VALUE])
    internal fun hello(): ResponseEntity<ByteArray> {
        return ResponseEntity
            .ok(ProtoBuf.encodeToByteArray(
                Result.success(message = "success", data = Person(name = "zs😫阿萨飒飒h", age = 18, gender = "man"))
            ))
    }
}