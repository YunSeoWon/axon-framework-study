# Messaging Concept



## Message

Messaging은 Axon에서 주요 개념 중 하나이다. MSA 환경 내의 모든 마이크로 서비스 사이의 커뮤니케이션은 Message 객체를 통해 이루어지기 때문이다. 

Axon에서 Message 객체는 Message 인터페이스를 이용하여 구현한다. 같은 Message 인터페이스로 구현하긴 하지만, 구현체 타입과 메시지를 다루는 방법은 구별된다. 

모든 Message는 payload, meta data, unique identifier를 가지고 있다. 



### Payload

payload는 해당 객체의 클래스 이름과 싣고있는 실제 데이터를 가진다. 



### Metadata

metadata는 메시지가 생성된 컨텍스트를 나타낸다. 예를 들어, metadata는 해당 메시지를 생성한 메시지에 대한 정보가 들어있을 수 있다. 

Axon에서 metadata는 `Map<String, Any>` 자료구조 형태로 되어있다. 또한, metadata는 한 번 정의되면 그 객체에 추가로 데이터를 집어넣을 수 없다(Immutable). 추가로 데이터를 집어넣으려면 metadata를 새로 만들어야 한다.

MetaData 객체는 .with()로 생성할 수 있다. 그리고 예제를 통해 and() 메서드는 그 객체를 복사하여 새로운 객체를 만들어 내는 것을 알 수 있다. and()를 호출한 metaData에는 objectkey에 대한 값이 들어있지 않고, and()로 인해 생성된 새로운 객체인 metaDataHavingObject에는 objectKey에 대한 값이 들어있다.

```kotlin
val metaData = MetaData.with("integerKey", 10)
  .and("stringKey", "value")

val metaDataHavingObject = metaData.and("objectKey", TestObject(1, "data"))

assertNull(metaData["objectKey"])
assertNotNull(metaDataHavingObject["objectKey"])
```



## Message Correlation

메시징 시스템에서는 일반적으로 메시지를 함께 그룹화하거나 상호 연관시킨다. Axon에서는 Command 메시지는 하나 이상의 이벤트 메시지를 생성할 수 있고, Query 메시지는 하나 이상의 QueryResponse를 생성할 수 있을 것이다.

**Unit of Work** 내에 생성된 새로운 메시지를 metadata에 채우기 위해서는, CorrelationDataProvider라는 것이 사용될 수 있다. Unit of work란 CorrelationDataProvider를 기반으로 새 메시지의 metadata를 채우는 것이다. Axon에서는 CorrelationDataProvider를 두 개의 인터페이스로 제공하고 있다.



### MessageOriginProvider

기본적으로, MessageOriginProvider는 사용할 CorrelationDataProvider로 등록된다. 이 객체는 Message가 다른 Message로 두 가지 값(correlationId, traceId)을 전달할 책임을 가지고 있다. CorrelationId는 항상 메시지가 생성된 메시지의 id를 참조한다. traceId는 메시지 체인을 시작한 메시지의 id를 참조한다. 새로운 메시지가 생성될 때, 두 개의 필드 모두 parent message에 존재하지 않으면 두가지 모두에 사용된다.(?)  



### SimpleCorrelationDataProvider

SimpleCorrelationDataProvider는 지정된 키 값을 한 메시지에서 다른 메시지의 메타데이터로 무조건 복사되도록 설정된다. SimpleCorrelationDataProvider의 생성자는 복사되어야 할 key의 리스트가 파라미터로 되어있다.

```kotlin
val provider = SimpleCorrelationDataProvider("copyId", "copyId2", "copyId3")
```



### MultiCorrelationDataProvider

MultiCorrelationDataProvider는 여러개의 CorrelationDataProvider의 효과를 조합할 때 유용하다. MultiCorrelationDataProvider의 생성자는 provider의 리스트가 파라미터로 되어있다.

```kotlin
val multiProvider = MultiCorrelationDataProvider<CommandMessage<*>>(
  listOf(
    SimpleCorrelationDataProvider("key"),
    MessageOriginProvider()
  )
)
```



### CorrelationDataProvider 커스터마이징

```kotlin
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
```





## Message Intercepting

Message Interceptor는 Dispatch interceptor와 handler interceptor로 총 두 가지 타입이 있다. 

Dispatch interceptor는 메시지가 메시지 핸들러에게 포착되기 전에 발생한다. 이 인터셉터는 핸들러가 존재하는지 안하는지 모른다. Handler Inteceptor는 메시지 핸들러가 호출되기 전에 호출된다. 



### Command Interceptors

커맨드 버스를 사용하는 장점 중 하나는 모든 들어오는 커맨드를 기반으로 어떤 조치를 취할 수 있다는 것이다. 보통 로깅이나 인증 관련 로직이 사용된다. 



#### Command Dispatch Interceptor

Message dispatch interceptor는 커맨드가 커맨드 버스에 포착될 때 호출된다. 이 인터셉터는 metadata를 추가함으로써 커맨드 메시지를 변경하는 능력을 가지고 있다. 또, 예외를 던져서 커맨드를 블락시킬 수도 있다. 이 인터셉터는 항상 커맨드를 포착한 쓰레드에서 호출된다.

Command Dispatch Interceptor를 만드는 방법은 다음과 같다. 아래 코드는 CommandBus에서 커맨드가 포착될 때 handle() 메서드가 호출된다.

```kotlin
class MyCommandDispatchInterceptor: MessageDispatchInterceptor<CommandMessage<*>> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handle(messages: MutableList<out CommandMessage<*>>): BiFunction<Int, CommandMessage<*>, CommandMessage<*>> {
        return BiFunction { index: Int?, command: CommandMessage<*> ->
            logger.info("Dispatching a command {}.", command)
            command
        }
    }
}
```



CommandBus에 Interceptor를 등록하는 방법은 다음과 같다.

```kotlin
@Configuration
class CommandBusConfiguration {
    @Bean
    fun commandBus(): CommandBus {
        return SimpleCommandBus.builder()
            .build()
            .apply {
                registerDispatchInterceptor(MyCommandDispatchInterceptor())
            }
    }
}
```



### Structural Validation

만약, 커맨드가 모든 필수 정보를 올바른 형식으로 담겨져 있지 않으면, 커맨드를 처리할 필요가 없다. (에러를 뱉는다거나 그런게 아니라..) 사실, 정보가 부족한(잘못된) 커맨드는 트랜잭션이 시작되기 전까지 가능한 한 빨리 블락시켜야한다. 그러므로, 인터셉터는 모든 들어오는 커맨드에 대해 검증 로직을 구현하는 것이 좋다. 이러한 것들을 **Structural Validation**이라고 한다.

Axon Framework는 JSR303 Bean Validation을 지원한다. 이건 나중에 알아보자~! (Optional)



### Command Handler Interceptors

Message Handler Interceptor는 커맨드 처리 전후로 어떤 행동을 취한다. 인터셉터는 여러 이유로 커맨드에 대한 작업들을 모두 블락시킬 수도 있다. 

인터셉터는 MessageHandlerInterceptor 인터페이스를 구현해야 한다. 이 인터페이스에는 handle 메서드가 정의되어 잇는데, UnitOfWork와 InterceptorChain이라는 파라미터를 가지고 있다. **InterceptorChain** 은 dispatching 작업을 계속하기 위해 사용된다. **UnitOfWork**는 핸들링 되어야 할 메시지를 주고, 메시지 핸들링을 하기 전, 하는 동안 또는 하는 후에 대해 논리적으로 연결될 수 있는 가능성을 제공한다(?)

Dispatch interceptor와는 다르게 Handler interceptor는 커맨드 핸들러의 컨텍스트를 호출한다. 즉, 핸들러와 핸들러 인터셉터는 처리 중인 메시지에 기반한 관련된 데이터를 unit of work에 첨부할 수 있다.

Handler Interceptor는 보통 커맨드를 핸들링하는 트랜잭션을 관리하는 데 사용된다. 그러기 위해서는 TransactionManager로 구성되는 TransactionManagingInterceptor를 등록한다. 

CommandHandlerInterceptor를 만들어보자. 아래 코드에서는 userId가 axonUser일 경우에만 핸들러가 핸들링 할 수 있도록 검증 로직을 구현해 놓은 것 같다.

```kotlin
class MyCommandHandlerInterceptor : MessageHandlerInterceptor<CommandMessage<*>> {

    override fun handle(unitOfWork: UnitOfWork<out CommandMessage<*>>, interceptorChain: InterceptorChain): Any? {
        val command = unitOfWork.message
        val userId = command.metaData["userId"] as? String ?: throw Exception()

        return if (userId == "axonUser") interceptorChain.proceed()
        else null
    }
}
```

CommandBus에 HandlerInterceptor를 등록하는 방법은 다음과 같다.

```kotlin
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
```

 

### @CommandHandlerInterceptor

Axon Framework에서는 애그리거트 / 엔티티의 메서드에 @CommandHandlerInterceptor 애노테이션을 적용시킴으로써 handler Interceptor를 추가할 수 있다. 애그리거트의 메서드와 일반적은 CommandHandlerInterceptor와의 차이점은 애노테이션 접근 방식을 사용하면, 주어진 애그리거크의 현재 상태에 따라 결정을 내릴 수 있다. 

애노테이션이 달린 Command handler interceptor의 일부 속성은 다음과 같다.

* 애노테이션은 애그리거트 내의 엔티티티에 놓을 수 있다.
* 커맨드 핸들러가 자식 엔티티에 있어도 커맨드를 애그리거트 루트 레벨에서 인터셉트 할 수 있다.
* 애노테이션으로 된 커맨드 핸들러 인터셉터에서 예외를 던짐으로써 커맨드 실행을 막을 수 있다.
* 커맨드 핸들러 인터셉터 메서드의 파라미터로써 InterceptorChain을 정의할 수 있고, chain을 커맨드 실행을 제어하기 위해 사용할 수 있다.
* @CommandHandlerInterceptor 애노테이션의 commandNamePattern 어트리뷰트를 사용함으로써 정규식에 매칭되는 모든 커맨드를 인터셉트 할 수 있다.
* 이벤트는 애노테이션으로 된 커맨드 핸들러 인터셉터로부터 적용될 수 있다.



예제 코드는 다음과 같다. 뭔가 애그리거트 단위에서 이벤트 로직을 관리할 수 있어서 좋은듯..?

```kotlin
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
```



### Event Interceptors

Command message와 비슷하게, Event message는 이벤트 발송전과 핸들링 하기 전에 추가 작업을 위해 인터셉트 될 수 있다. Event Interceptor도 Dispatch Interceptor와 Handler Interceptor 두 가지 타입이 있다.



#### Event Dispatch Interceptor

이벤트버스에 등록된 모든 메시지 디스패처들은 이벤트가 발행될 때 호출된다. 이 인터셉터들은 metadata를 추가함으로써 이벤트 메시지를 변경하는 능력을 가지고 있다. 또한, 인터셉터는 이벤트가 발행될 때 전체 로깅 기능을 제공할 수 있다. 이 인터셉터들은 이벤트가 발행한 스레드에서 호출된다.

Event dispatch interceptor 구현 방법은 다음과 같다.

```kotlin
class EventLoggingDispatcherInterceptor : MessageDispatchInterceptor<EventMessage<*>> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handle(messages: MutableList<out EventMessage<*>>?): BiFunction<Int, EventMessage<*>, EventMessage<*>> {
        return BiFunction { index, event ->
            logger.info("Publishing event: [{}].${event}")
            event
        }
    }
}
```

이 인터셉터를 이벤트 버스에 붙이는 방법은 다음과 같다.

```kotlin
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
```





#### Event Handler Interceptor

Event handler interceptor는 이벤트 처리 전후로 어떤 행동을 취할 수 있다. 

인터셉터는 MessageHandlerInterceptor 인터페이스를 구현해야 한다. 이 인터페이스에는 handle() 메서드가 있는데 UnitOfWork와 InterceptorChain 이라는 파라미터가 있다. **InterceptorChain** 은 dispatching 작업을 계속하기 위해 사용된다. **UnitOfWork**는 핸들링 되어야 할 메시지를 주고, 메시지 핸들링을 하기 전, 하는 동안 또는 하는 후에 대해 논리적으로 연결될 수 있는 가능성을 제공한다(?)

Event Handler Interceptor를 만드는 방법은 다음과 같다.

```kotlin
class MyEventHandlerInterceptor : MessageHandlerInterceptor<EventMessage<*>> {
    
    override fun handle(unitOfWork: UnitOfWork<out EventMessage<*>>, interceptorChain: InterceptorChain): Any? {
        val event = unitOfWork.message
        val userId = event.metaData["userId"] as? String ?: throw Exception()
        
        return if (userId == "axonUser") interceptorChain.proceed()
        else null
    }
}
```

핸들러 인터셉터를 EventProcessor에 등록하는 방법은 다음과 같다.

```kotlin
class EventProcessorConfiguration {
    
    fun configureEventProcessing(configurer: Configurer) {
        configurer.eventProcessing()
            .registerTrackingEventProcessor("my-tracking-processor")
            .registerHandlerInterceptor("my-tracking-processor") {
                MyEventHandlerInterceptor()
            }
    }
}
```



### Query Interceptor

Query Bus를 사용하는 장점 중 하나는 모든 들어오는 쿼리에 기반하여 어떤 조치를 취할 수 있다는 것이다. 이러한 행동들은 인터셉터를 이용하여 행해진다.



### Query Dispatch Interceptor

Query dispatch interceptor는 쿼리가 query bus에 포착되거나 쿼리 메시지에 대한 구독 업데이트가 쿼리 업데이트 emitter에 의해 포착될 때 호출된다. 이 인터셉터는 metadata를 추가함으로써 메시지를 변경하는 능력을 가지고 있다. 또, 예외를 발생시킴으로써 핸들러 실행을 블럭시킬 수 있다. 이 인터셉터는 항상 메시지를 포착한 쓰레드에서 호출된다.



#### Query Handler Interceptor

Query handler interceptor는 쿼리 과정 전후로 행동을 취할 수 있다. 인터셉터는 여러 가지 이유로 모든 쿼리 과정을 블럭시킬 수도 있다.

인터셉터는 MessageHandlerInterceptor로 구현해야 한다. 



## @MessageHandlerInterceptor

MessageHandlerInterceptor는 핸들러를 포함하는 특정 구성요소에 대한 핸들러 인터셉터를 정의할 수 있다. 이는 @MessageHandler와 결합하여 메시지를 처리하는 방법을 추가함으로써 구현할 수 있다.

MessageHandlerInterceptor를 구현하는 방법은 총 세가지이다.

1. MessageHandlerInterceptor는 체인의 다른 인터셉터를 진행할 시기를 결정하기 위해 InterceptorChain과 같이 동작한다. InterceptorChain은 optional이다. 
2. 인터셉터가 처리할 메시지의 타입도 정할 수 있다. 기본적으로는 메시지 인터페이스의 모든 타입들에 대해서 반응할 것이다. EventMessage 특정 인터셉터를 원하는 경우에는 애노테이션으로 설정할 수 있다.
3. 어떤 메시지가 인터셉터에 의해 반응해야 하는지를 조금 더 세밀하게 제어하기 위해 처리할 메시지에 포함된 payloadtype을 지정할 수 있다.

```kotlin
@MessageHandlerInterceptor
fun intercept(message: Message<*>) {
	// logic
}

@MessageHandlerInterceptor(messageType = EventMessage::class)
fun intercept(message: Message<*>) {
	// logic
}

@MessageHandlerInterceptor(messageType = QueryMessage::class)
fun intercept(queryMessage: QueryMessage<*, *>, interceptorChain: InterceptorChain) {
  // logic
}
```



### @ExceptionHandler

@MessageHandlerInterceptor는 인터셉트 기능에 보다 구체적인 버전을 사용할 수 있다. @ExceptionHandler 애노테이션 메서드는 예외가 발생할 때만 메서드가 호출된다. 	

```kotlin
@ExceptionHandler(resultType = IllegalArgumentException::class)
fun handle(exception: IllegalArgumentException) {
    // logic
}
```





## Command

Command는 애플리케이션의 상태를 변경할 용도로 사용된다. 커맨드 객체는 POJO 객체이고, CommandMessag 구현체를 사용하여 감싸지게 된다.

커맨드는 정확히 한 방향으로 통신된다. 송신 측은 어떤 컴포넌트로 커맨드를 보내는지, 커맨드의 위치 등을 알지 못하지만, 커맨드를 핸들링 한 결과를 알 수 있다. 커맨드 버스를 통해 전송된 커맨드 메시지가 결과를 반환할 수 있기 때문이다.



## Event

Event는 애플리케이션에서 발생한 일을 설명하는 객체이다. 이벤트의 원인은 애그리거트이다. 어떤 중요한 일이 애그리거트에 의해 발생했다면, 이벤트를 발생시킨다. Axon Framework에서는, 이벤트는 모든 객체가 될 수 있다. (대신 Serializable한 객체이면 더 좋다.)

이벤트가 감지된다면, Axon은 이벤트 객체를 EventMEssage 객체에 감싼다. 사용되는 실제 메시지의 타입은 이벤트의 출처에 따라 다르다. 이벤트가 애그리거트에 의해 생겨났다면, 메시지 타입은 DomainEventMessage이고, 다른 이벤트들은 EventMessage 타입이다.

원래 이벤트 객체는 EventMessage의 payload에 저장된다. 그 다음, EventMessage의 메타데이터에 정보를 저장할 수 있다. 메타데이터에 데이터를 저장할 때는 이벤트에 관한 추가 정보를 저장하는 용도로 사용되어야 한다. 

될 수 있다면 도메인 이벤트 객체는 불변 객체로 디자인 하면 좋다. 

이벤트를 이벤트 버스에서 포착한다면, 이 이벤트 객체를 EventMessage 객체에 감싸야 한다. GenericEventMessage는 EventMessage의 구현체인데 보통 이 객체를 통해 이벤트 객체를 감싼다. 