package com.example.axon.application.interceptor

import org.axonframework.eventhandling.EventMessage
import org.axonframework.messaging.MessageDispatchInterceptor
import org.slf4j.LoggerFactory
import java.util.function.BiFunction

class EventLoggingDispatcherInterceptor : MessageDispatchInterceptor<EventMessage<*>> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handle(messages: MutableList<out EventMessage<*>>?): BiFunction<Int, EventMessage<*>, EventMessage<*>> {
        return BiFunction { index, event ->
            logger.info("Publishing event: [{}].${event}")
            event
        }
    }
}