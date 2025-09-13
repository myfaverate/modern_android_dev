package edu.tyut.kotlin_protobuf.controller

import edu.tyut.kotlin_protobuf.bean.Person
import edu.tyut.kotlin_protobuf.bean.Result
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
private final class HelloController {

    private final val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @OptIn(markerClass = [ExperimentalSerializationApi::class])
    @GetMapping(value = ["/hello"], produces = [MediaType.APPLICATION_PROTOBUF_VALUE])
    private final fun hello(): ResponseEntity<ByteArray> {
        return ResponseEntity
            .ok(ProtoBuf.encodeToByteArray(
                Result.success(message = "success", data = Person(name = "zs😫阿萨飒飒h", age = 18, gender = "man"))
            ))
    }
    @GetMapping(value = ["/hello1"])
    internal fun hello1(
        @RequestHeader headers: Map<String,String>,
    ): String {
        logger.info("headers: $headers")
        return "Hello World 世界！"
    }
}