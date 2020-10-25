package com.example.axon.presentation.router

import com.example.axon.presentation.handler.TestHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class TestRouter (
    private val testHandler: TestHandler
) {
    @Bean
    fun testRoute(): RouterFunction<ServerResponse> {
        return coRouter {
            "tests/".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    GET("", testHandler::test)
                }
            }
        }
    }
}