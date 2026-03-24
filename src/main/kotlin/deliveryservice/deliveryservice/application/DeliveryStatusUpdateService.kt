package deliveryservice.deliveryservice.application

import deliveryservice.deliveryservice.adapter.persistence.DeliveryRepository
import deliveryservice.deliveryservice.adapter.persistence.DriverRepository
import deliveryservice.deliveryservice.adapter.persistence.OutboxRepository
import deliveryservice.deliveryservice.adapter.redis.RedisDeliveryStatusStore
import deliveryservice.deliveryservice.application.provided.DeliveryStatusUpdater
import deliveryservice.deliveryservice.domain.delivery.DeliveryStatus
import deliveryservice.deliveryservice.domain.outbox.OutboxEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

/**
 * 배달 상태 변경 서비스.
 *
 * AllMart 문제 해결:
 * - COMPLETED/CANCELLED 시 driver.releaseDelivery() 반드시 호출 (누락 버그 수정)
 * - Outbox INSERT: Order Service에 delivery.completed.v1 발행 → Order.markAsDelivered() 트리거
 */
@Service
@Transactional
class DeliveryStatusUpdateService(
    private val deliveryRepository: DeliveryRepository,
    private val driverRepository: DriverRepository,
    private val outboxRepository: OutboxRepository,
    private val statusStore: RedisDeliveryStatusStore,
    private val objectMapper: ObjectMapper,
) : DeliveryStatusUpdater {

    override fun updateStatus(deliveryId: Long, newStatus: DeliveryStatus) {
        val delivery = deliveryRepository.findById(deliveryId).orElse(null)
            ?: throw NoSuchElementException("배달을 찾을 수 없습니다: $deliveryId")

        val oldStatus = delivery.status

        when (newStatus) {
            DeliveryStatus.IN_PROGRESS -> delivery.startDelivery()
            DeliveryStatus.COMPLETED   -> delivery.complete()
            DeliveryStatus.CANCELLED   -> delivery.cancel()
            else -> throw IllegalArgumentException("직접 전환 불가 상태: $newStatus")
        }

        deliveryRepository.save(delivery)

        // Redis 카운트 조정
        statusStore.decrement(oldStatus)
        statusStore.increment(newStatus)

        // COMPLETED/CANCELLED → 드라이버 카운트 감소 (AllMart 누락 버그 수정)
        if (newStatus == DeliveryStatus.COMPLETED || newStatus == DeliveryStatus.CANCELLED) {
            val driver = driverRepository.findById(delivery.driverId).orElse(null)
                ?: throw NoSuchElementException("드라이버를 찾을 수 없습니다: ${delivery.driverId}")
            driver.releaseDelivery()
            driverRepository.save(driver)

            // Outbox INSERT → Debezium → Kafka → Order Service 수신
            val eventType = if (newStatus == DeliveryStatus.COMPLETED) "delivery.completed.v1" else "delivery.cancelled.v1"
            val payload = objectMapper.writeValueAsString(
                mapOf(
                    "deliveryId" to delivery.id,
                    "orderId"    to delivery.orderId,
                    "status"     to delivery.status.name,
                )
            )
            outboxRepository.save(
                OutboxEvent.of(
                    eventType     = eventType,
                    aggregateType = "DELIVERY",
                    aggregateId   = delivery.id.toString(),
                    payload       = payload,
                )
            )
        }
    }
}