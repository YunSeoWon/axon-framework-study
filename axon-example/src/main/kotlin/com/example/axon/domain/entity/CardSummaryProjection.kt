package com.example.axon.domain.entity

import org.axonframework.eventhandling.EventMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.Message
import org.axonframework.messaging.interceptors.ExceptionHandler
import org.axonframework.messaging.interceptors.MessageHandlerInterceptor
import org.axonframework.queryhandling.QueryMessage

class CardSummaryProjection {

    @MessageHandlerInterceptor(messageType = QueryMessage::class)
    fun intercept(queryMessage: QueryMessage<*, *>, interceptorChain: InterceptorChain) {
        interceptorChain.proceed()
    }

    @ExceptionHandler(resultType = IllegalArgumentException::class)
    fun handle(exception: IllegalArgumentException) {
        // logic
    }
}