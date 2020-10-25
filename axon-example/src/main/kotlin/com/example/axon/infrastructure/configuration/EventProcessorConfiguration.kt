package com.example.axon.infrastructure.configuration

import com.example.axon.application.interceptor.MyEventHandlerInterceptor
import org.axonframework.config.Configurer

class EventProcessorConfiguration {

    fun configureEventProcessing(configurer: Configurer) {
        configurer.eventProcessing()
            .registerTrackingEventProcessor("my-tracking-processor")
            .registerHandlerInterceptor("my-tracking-processor") {
                MyEventHandlerInterceptor()
            }
    }
}