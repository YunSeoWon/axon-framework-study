package com.example.axon.infrastructure.configuration

import com.example.axon.application.interceptor.EventLoggingDispatcherInterceptor
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore
import org.axonframework.eventsourcing.eventstore.EventStorageEngine
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventBusConfiguration {

    @Bean
    fun eventBus(eventStorageEngine: EventStorageEngine): EventBus {
        return EmbeddedEventStore.builder()
            .storageEngine(eventStorageEngine)
            .build()
            .apply {
                registerDispatchInterceptor(EventLoggingDispatcherInterceptor())
            }
    }
}