package deliveryservice.deliveryservice.domain.delivery

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

/**
 * Delivery Aggregate Root.
 *
 * - driverId: Driver는 ID로만 참조 (Aggregate 간 직접 참조 금지)
 * - private constructor + @PersistenceCreator: 팩토리 메서드로만 생성 강제
 */
@Table("deliveries")
class Delivery @PersistenceCreator private constructor(
    @Id val id: Long? = null,
    val orderId: Long,
    val driverId: Long,
    var status: DeliveryStatus = DeliveryStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var completedAt: LocalDateTime? = null,
    var cancelledAt: LocalDateTime? = null,
) {
    companion object {
        fun create(orderId: Long, driverId: Long): Delivery {
            require(orderId > 0) { "orderId는 양수여야 합니다." }
            require(driverId > 0) { "driverId는 양수여야 합니다." }
            return Delivery(orderId = orderId, driverId = driverId)
        }
    }

    fun startDelivery(): Delivery {
        require(status.canTransitionTo(DeliveryStatus.IN_PROGRESS)) {
            "배달 상태를 ${status} → IN_PROGRESS로 전환할 수 없습니다."
        }
        status = DeliveryStatus.IN_PROGRESS
        return this
    }

    fun complete(): Delivery {
        require(status.canTransitionTo(DeliveryStatus.COMPLETED)) {
            "배달 상태를 ${status} → COMPLETED로 전환할 수 없습니다."
        }
        status = DeliveryStatus.COMPLETED
        completedAt = LocalDateTime.now()
        return this
    }

    fun cancel(): Delivery {
        require(status.canTransitionTo(DeliveryStatus.CANCELLED)) {
            "배달 상태를 ${status} → CANCELLED로 전환할 수 없습니다."
        }
        status = DeliveryStatus.CANCELLED
        cancelledAt = LocalDateTime.now()
        return this
    }
}