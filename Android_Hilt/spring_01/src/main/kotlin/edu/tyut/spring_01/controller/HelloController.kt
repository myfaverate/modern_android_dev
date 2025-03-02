package edu.tyut.spring_01.controller

import edu.tyut.spring_01.bean.Person
import edu.tyut.spring_01.bean.Result
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.text.SimpleDateFormat
import java.util.TimeZone

@RestController
class HelloController {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @GetMapping(value = ["/hello"])
    fun getHello(): String {
        return "Hello World 世界!"
    }
    @GetMapping(value = ["/getPerson"])
    fun getPerson(): Result {
        return Result.success<Person>(message = "success", data = Person(name = "zsh", age = 12, gender = "男")).apply {
            logger.info("person is $this")
        }
    }

    @GetMapping(value = ["/login"])
    fun login(): Result {
        val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        logger.info("login success -> time: {}", simpleDateFormat.format(System.currentTimeMillis()))
        return Result.success<Person>(message = "success", data = Person(name = "login成功", age = 12, gender = "男"))
    }
}