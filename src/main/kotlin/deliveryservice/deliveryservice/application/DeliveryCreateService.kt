package deliveryservice.deliveryservice.application

import deliveryservice.deliveryservice.adapter.persistence.DeliveryRepository
import deliveryservice.deliveryservice.adapter.persistence.DriverRepository
import deliveryservice.deliveryservice.adapter.persistence.OutboxRepository
import deliveryservice.deliveryservice.adapter.redis.RedisDeliveryStatusStore
import deliveryservice.deliveryservice.application.provided.DeliveryCreator
import deliveryservice.deliveryservice.domain.delivery.Delivery
import deliveryservice.deliveryservice.domain.delivery.DeliveryStatus
import deliveryservice.deliveryservice.domain.outbox.OutboxEvent
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

/**
 * 배달 생성 서비스.
 *
 * AllMart 문제 해결:
 * - PESSIMISTIC_WRITE(DB 락) → Redisson RLock(분산 락): DB 커넥션 점유 없이 동시성 제어
 * - Outbox INSERT only: Delivery + OutboxEvent 같은 트랜잭션 → Debezium이 delivery.assigned.v1 발행
 *
 * 락 전략:
 * - 드라이버 목록을 먼저 조회(락 없이) → 후보 중 첫 번째 드라이버에 RLock 획득
 * - 락 내부에서 canAcceptDelivery() 재검증 (TOCTOU 방어)
 */
@Service
@Transactional
class DeliveryCreateService(
    private val driverRepository: DriverRepository,
    private val deliveryRepository: DeliveryRepository,
    private val outboxRepository: OutboxRepository,
    private val statusStore: RedisDeliveryStatusStore,
    private val redissonClient: RedissonClient,
    private val objectMapper: ObjectMapper,
) : DeliveryCreator {

    override fun create(orderId: Long): Long {
        val availableDrivers = driverRepository.findAvailable()
        check(availableDrivers.isNotEmpty()) { "배정 가능한 드라이버가 없습니다." }

        for (driver in availableDrivers) {
            val lock = redissonClient.getLock("driver:assign:${driver.id}")
            if (!lock.tryLock()) continue

            try {
                // 락 내부 재검증 — 동시 요청이 같은 드라이버를 노릴 때 방어
                val freshDriver = driverRepository.findById(driver.id!!).orElse(null)
                    ?: continue
                if (!freshDriver.canAcceptDelivery()) continue

                freshDriver.assignDelivery()
                driverRepository.save(freshDriver)

                val delivery = Delivery.create(orderId = orderId, driverId = freshDriver.id!!)
                val savedDelivery = deliveryRepository.save(delivery)

                val payload = objectMapper.writeValueAsString(
                    mapOf(
                        "deliveryId" to savedDelivery.id,
                        "orderId"    to savedDelivery.orderId,
                        "driverId"   to savedDelivery.driverId,
                        "status"     to savedDelivery.status.name,
                    )
                )
                outboxRepository.save(
                    OutboxEvent.of(
                        eventType     = "delivery.assigned.v1",
                        aggregateType = "DELIVERY",
                        aggregateId   = savedDelivery.id.toString(),
                        payload       = payload,
                    )
                )

                statusStore.increment(DeliveryStatus.PENDING)
                return savedDelivery.id!!

            } finally {
                if (lock.isHeldByCurrentThread) lock.unlock()
            }
        }

        error("모든 드라이버 배정 시도 실패 — 동시 요청으로 인한 경쟁 상황")
    }
}