package com.example.axon.application.interceptor

import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageDispatchInterceptor
import org.slf4j.LoggerFactory
import java.util.function.BiFunction

class MyCommandDispatchInterceptor: MessageDispatchInterceptor<CommandMessage<*>> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handle(messages: MutableList<out CommandMessage<*>>): BiFunction<Int, CommandMessage<*>, CommandMessage<*>> {
        return BiFunction { index: Int?, command: CommandMessage<*> ->
            logger.info("Dispatching a command {}.", command)
            command
        }
    }
}