package com.example.axon.presentation.handler

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyValueAndAwait

@Component
class TestHandler {

    suspend fun test(request: ServerRequest): ServerResponse {
        return ok().bodyValueAndAwait("")
    }
}