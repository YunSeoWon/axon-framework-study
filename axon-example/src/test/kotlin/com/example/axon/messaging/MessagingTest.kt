package com.example.axon.messaging

import io.mockk.junit5.MockKExtension
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.GenericEventMessage
import org.axonframework.messaging.Message
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Collections.singletonMap
import java.util.function.Function

@ExtendWith(MockKExtension::class)
class MessagingTest {

    @Test
    fun `Metadata 테스트`() {
        val metaData = MetaData.with("integerKey", 10)
            .and("stringKey", "value")

        val metaDataHavingObject = metaData.and("objectKey", TestObject(1, "data"))

        assertNull(metaData["objectKey"])
        assertNotNull(metaDataHavingObject["objectKey"])

        val eventMessage = GenericEventMessage
            .asEventMessage<String>("myPayload")
            .withMetaData(singletonMap("myKey", 42))
            .andMetaData(singletonMap("otherKey", "someValue"))
    }

    @Test
    fun providerTest() {
        val provider = SimpleCorrelationDataProvider("copyId", "copyId2", "copyId3")
        val multiProvider = MultiCorrelationDataProvider<CommandMessage<*>>(
            listOf(
                SimpleCorrelationDataProvider("key"),
                MessageOriginProvider()
            )
        )
    }

}

data class TestObject(
    val testId: Int,
    val testData: String
)

class AuthCorrelationDataProvider(
    private val usernameProvider: Function<String, String>
): CorrelationDataProvider {

    override fun correlationDataFor(message: Message<*>?): MutableMap<String, *> {
        return if(message is CommandMessage<*> && message.metaData.containsKey("authorization")) {
            val token = message.metaData["authorization"] as String
            hashMapOf("username" to usernameProvider.apply(token))
        } else hashMapOf()
    }
}