package com.example.axon.infrastructure.configuration

import com.example.axon.application.interceptor.MyCommandDispatchInterceptor
import com.example.axon.application.interceptor.MyCommandHandlerInterceptor
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.SimpleCommandBus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommandBusConfiguration {
    @Bean
    fun commandBus(): CommandBus {
        return SimpleCommandBus.builder()
            .build()
            .apply {
                registerDispatchInterceptor(MyCommandDispatchInterceptor())
                registerHandlerInterceptor(MyCommandHandlerInterceptor())
            }
    }
}