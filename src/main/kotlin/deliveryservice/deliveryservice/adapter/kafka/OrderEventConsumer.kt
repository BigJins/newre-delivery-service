package deliveryservice.deliveryservice.adapter.kafka

import deliveryservice.deliveryservice.application.provided.DeliveryCreator
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper

/**
 * Order Service → Delivery Service 이벤트 수신.
 *
 * order.created.v1 토픽: Debezium EventRouter가 outbox_event.payload를 메시지 값으로 전달.
 * ack-mode: record → 처리 완료 후 수동 커밋 (AllMart auto-commit 메시지 유실 수정)
 */
@Component
class OrderEventConsumer(
    private val deliveryCreator: DeliveryCreator,
    private val objectMapper: ObjectMapper,
) {
    @KafkaListener(topics = ["order.created.v1"], groupId = "delivery-service")
    fun handleOrderCreated(message: String) {
        val payload = objectMapper.readTree(message)
        val orderId = payload["orderId"].asLong()
        deliveryCreator.create(orderId)
    }
}