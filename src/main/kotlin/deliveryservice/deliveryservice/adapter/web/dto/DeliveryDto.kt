package deliveryservice.deliveryservice.adapter.web.dto

import deliveryservice.deliveryservice.domain.delivery.Delivery
import deliveryservice.deliveryservice.domain.delivery.DeliveryStatus
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class UpdateDeliveryStatusRequest(
    val status: DeliveryStatus,
)

data class DeliveryResponse(
    val deliveryId: Long,
    val orderId: Long,
    val driverId: Long,
    val status: String,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val cancelledAt: LocalDateTime?,
) {
    companion object {
        fun from(delivery: Delivery) = DeliveryResponse(
            deliveryId  = delivery.id!!,
            orderId     = delivery.orderId,
            driverId    = delivery.driverId,
            status      = delivery.status.name,
            createdAt   = delivery.createdAt,
            completedAt = delivery.completedAt,
            cancelledAt = delivery.cancelledAt,
        )
    }
}