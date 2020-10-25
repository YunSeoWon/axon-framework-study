package com.example.axon.application.interceptor

import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork

class MyCommandHandlerInterceptor : MessageHandlerInterceptor<CommandMessage<*>> {

    override fun handle(unitOfWork: UnitOfWork<out CommandMessage<*>>, interceptorChain: InterceptorChain): Any? {
        val command = unitOfWork.message
        val userId = command.metaData["userId"] as? String ?: throw Exception()

        return if (userId == "axonUser") interceptorChain.proceed()
        else null
    }
}