package deliveryservice.deliveryservice.domain.outbox

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * INSERT only — UPDATE/DELETE 절대 금지.
 * Delivery 저장과 반드시 같은 트랜잭션에서 INSERT.
 * Debezium이 binlog 감지 → Kafka 발행.
 */
@Table("outbox_event")
data class OutboxEvent @PersistenceCreator private constructor(
    @Id val id: Long? = null,
    val eventType: String,
    val aggregateType: String,
    val aggregateId: String,
    val payload: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun of(
            eventType: String,
            aggregateType: String,
            aggregateId: String,
            payload: String,
        ): OutboxEvent = OutboxEvent(
            eventType = eventType,
            aggregateType = aggregateType,
            aggregateId = aggregateId,
            payload = payload,
        )
    }
}