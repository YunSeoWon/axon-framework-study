package com.example.axon.domain.entity

import org.axonframework.messaging.InterceptorChain
import org.axonframework.modelling.command.CommandHandlerInterceptor

data class GiftCard(
    val state: String
) {

    @CommandHandlerInterceptor
    fun intercept(command: RedeemCardCommand, interceptorChain: InterceptorChain) {
        if (state == command.state) {
            interceptorChain.proceed()
        }
    }
}

data class RedeemCardCommand(
    val state: String
)