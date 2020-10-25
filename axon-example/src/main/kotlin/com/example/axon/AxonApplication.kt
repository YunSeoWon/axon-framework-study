package com.example.axon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@EnableWebFlux
@SpringBootApplication
class AxonApplication

fun main(args: Array<String>) {
    runApplication<AxonApplication>(*args)
}
