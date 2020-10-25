package com.example.axon.application

import org.axonframework.messaging.MetaData
import org.springframework.stereotype.Component

@Component
class TestService {

    fun test() {
        val metaData = MetaData.with("key", 1)
            .and("stringKey", "value")

        metaData.and("add", "add")
        
    }
}