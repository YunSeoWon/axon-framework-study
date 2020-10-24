# Messaging Concept



## Message

Messaging은 Axon에서 주요 개념 중 하나이다. MSA 환경 내의 모든 마이크로 서비스 사이의 커뮤니케이션은 Message 객체를 통해 이루어지기 때문이다. 

Axon에서 Message 객체는 Message 인터페이스를 이용하여 구현한다. 같은 Message 인터페이스로 구현하긴 하지만, 구현체 타입과 메시지를 다루는 방법은 구별된다. 

모든 Message는 payload, meta data, unique identifier를 가지고 있다. 



### Payload

payload는 해당 객체의 클래스 이름과 싣고있는 실제 데이터를 가진다. 



### Metadata

metadata는 메시지가 생성된 컨텍스트를 나타낸다. 예를 들어, metadata는 해당 메시지를 생성한 메시지에 대한 정보가 들어있을 수 있다. 

Axon에서 metadata는 `Map<String, Any>` 자료구조 형태로 되어있다. 





## Command

Command는 애플리케이션의 상태를 변경할 용도로 사용된다. 커맨드 객체는 POJO 객체이고, CommandMessag 구현체를 사용하여 감싸지게 된다.

커맨드는 정확히 한 방향으로 통신된다. 송신 측은 어떤 컴포넌트로 커맨드를 보내는지, 커맨드의 위치 등을 알지 못하지만, 커맨드를 핸들링 한 결과를 알 수 있다. 커맨드 버스를 통해 전송된 커맨드 메시지가 결과를 반환할 수 있기 때문이다.



## Event

Event는 애플리케이션에서 발생한 일을 설명하는 객체이다. 이벤트의 원인은 애그리거트이다. 어떤 중요한 일이 애그리거트에 의해 발생했다면, 이벤트를 발생시킨다. Axon Framework에서는, 이벤트는 모든 객체가 될 수 있다. (대신 Serializable한 객체이면 더 좋다.)

이벤트가 감지된다면, Axon은 이벤트 객체를 EventMEssage 객체에 감싼다. 사용되는 실제 메시지의 타입은 이벤트의 출처에 따라 다르다. 이벤트가 애그리거트에 의해 생겨났다면, 메시지 타입은 DomainEventMessage이고, 다른 이벤트들은 EventMessage 타입이다.

원래 이벤트 객체는 EventMessage의 payload에 저장된다. 그 다음, EventMessage의 메타데이터에 정보를 저장할 수 있다. 메타데이터에 데이터를 저장할 때는 이벤트에 관한 추가 정보를 저장하는 용도로 사용되어야 한다. 

될 수 있다면 도메인 이벤트 객체는 불변 객체로 디자인 하면 좋다. 

이벤트를 이벤트 버스에서 포착한다면, 이 이벤트 객체를 EventMessage 객체에 감싸야 한다. GenericEventMessage는 EventMessage의 구현체인데 보통 이 객체를 통해 이벤트 객체를 감싼다. 