package com.example.axon.application.interceptor

import org.axonframework.eventhandling.EventMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork

class MyEventHandlerInterceptor : MessageHandlerInterceptor<EventMessage<*>> {

    override fun handle(unitOfWork: UnitOfWork<out EventMessage<*>>, interceptorChain: InterceptorChain): Any? {
        val event = unitOfWork.message
        val userId = event.metaData["userId"] as? String ?: throw Exception()

        return if (userId == "axonUser") interceptorChain.proceed()
        else null
    }
}