package deliveryservice.deliveryservice.application

import deliveryservice.deliveryservice.adapter.persistence.DeliveryRepository
import deliveryservice.deliveryservice.adapter.persistence.DriverRepository
import deliveryservice.deliveryservice.adapter.persistence.OutboxRepository
import deliveryservice.deliveryservice.adapter.redis.RedisDeliveryStatusStore
import deliveryservice.deliveryservice.domain.delivery.Delivery
import deliveryservice.deliveryservice.domain.delivery.DeliveryStatus
import deliveryservice.deliveryservice.domain.driver.Driver
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import tools.jackson.databind.ObjectMapper
import java.util.Optional

class DeliveryStatusUpdateServiceTest : BehaviorSpec({

    val deliveryRepository = mockk<DeliveryRepository>()
    val driverRepository   = mockk<DriverRepository>()
    val outboxRepository   = mockk<OutboxRepository>()
    val statusStore        = mockk<RedisDeliveryStatusStore>()
    val objectMapper       = mockk<ObjectMapper>()

    val service = DeliveryStatusUpdateService(
        deliveryRepository, driverRepository, outboxRepository, statusStore, objectMapper
    )

    afterEach { clearAllMocks() }

    given("PENDING 배달을 IN_PROGRESS로 전환") {
        val delivery = Delivery.create(orderId = 1L, driverId = 10L)  // PENDING

        beforeEach {
            every { deliveryRepository.findById(1L) } returns Optional.of(delivery)
            every { deliveryRepository.save(any()) } returnsArgument 0
            every { statusStore.decrement(any()) } returns Unit
            every { statusStore.increment(any()) } returns Unit
        }

        `when`("updateStatus(1L, IN_PROGRESS)") {
            then("delivery 저장, Redis 카운트 조정, Outbox/Driver 미호출") {
                service.updateStatus(1L, DeliveryStatus.IN_PROGRESS)

                verify(exactly = 1) { deliveryRepository.save(any()) }
                verify(exactly = 1) { statusStore.decrement(DeliveryStatus.PENDING) }
                verify(exactly = 1) { statusStore.increment(DeliveryStatus.IN_PROGRESS) }
                verify(exactly = 0) { driverRepository.save(any()) }
                verify(exactly = 0) { outboxRepository.save(any()) }
            }
        }
    }

    given("IN_PROGRESS 배달을 COMPLETED로 전환 — AllMart 누락 버그 수정") {
        val delivery = Delivery.create(orderId = 1L, driverId = 10L).also { it.startDelivery() }
        val driver   = Driver.create("홍길동", 3).also { it.assignDelivery() }

        beforeEach {
            every { deliveryRepository.findById(1L) } returns Optional.of(delivery)
            every { deliveryRepository.save(any()) } returnsArgument 0
            every { driverRepository.findById(10L) } returns Optional.of(driver)
            every { driverRepository.save(any()) } returnsArgument 0
            every { statusStore.decrement(any()) } returns Unit
            every { statusStore.increment(any()) } returns Unit
            every { objectMapper.writeValueAsString(any()) } returns """{"deliveryId":1}"""
            every { outboxRepository.save(any()) } returnsArgument 0
        }

        `when`("updateStatus(1L, COMPLETED)") {
            then("driver.releaseDelivery() 호출, outbox delivery.completed.v1 저장") {
                service.updateStatus(1L, DeliveryStatus.COMPLETED)

                verify(exactly = 1) { deliveryRepository.save(any()) }
                verify(exactly = 1) { driverRepository.save(any()) }
                verify(exactly = 1) { statusStore.decrement(DeliveryStatus.IN_PROGRESS) }
                verify(exactly = 1) { statusStore.increment(DeliveryStatus.COMPLETED) }
                verify(exactly = 1) {
                    outboxRepository.save(match { it.eventType == "delivery.completed.v1" })
                }
            }
        }
    }

    given("PENDING 배달을 CANCELLED로 전환") {
        val delivery = Delivery.create(orderId = 1L, driverId = 10L)  // PENDING
        val driver   = Driver.create("홍길동", 3).also { it.assignDelivery() }

        beforeEach {
            every { deliveryRepository.findById(1L) } returns Optional.of(delivery)
            every { deliveryRepository.save(any()) } returnsArgument 0
            every { driverRepository.findById(10L) } returns Optional.of(driver)
            every { driverRepository.save(any()) } returnsArgument 0
            every { statusStore.decrement(any()) } returns Unit
            every { statusStore.increment(any()) } returns Unit
            every { objectMapper.writeValueAsString(any()) } returns """{"deliveryId":1}"""
            every { outboxRepository.save(any()) } returnsArgument 0
        }

        `when`("updateStatus(1L, CANCELLED)") {
            then("driver.releaseDelivery() 호출, outbox delivery.cancelled.v1 저장") {
                service.updateStatus(1L, DeliveryStatus.CANCELLED)

                verify(exactly = 1) { deliveryRepository.save(any()) }
                verify(exactly = 1) { driverRepository.save(any()) }
                verify(exactly = 1) {
                    outboxRepository.save(match { it.eventType == "delivery.cancelled.v1" })
                }
            }
        }
    }

    given("존재하지 않는 배달 ID") {
        `when`("updateStatus(999L, IN_PROGRESS)") {
            then("NoSuchElementException") {
                every { deliveryRepository.findById(999L) } returns Optional.empty()
                shouldThrow<NoSuchElementException> {
                    service.updateStatus(999L, DeliveryStatus.IN_PROGRESS)
                }
                verify(exactly = 0) { deliveryRepository.save(any()) }
            }
        }
    }

    given("잘못된 상태 전환 (PENDING → COMPLETED)") {
        val delivery = Delivery.create(orderId = 1L, driverId = 10L)  // PENDING

        `when`("updateStatus(1L, COMPLETED)") {
            then("IllegalArgumentException (도메인 canTransitionTo 실패)") {
                every { deliveryRepository.findById(1L) } returns Optional.of(delivery)
                shouldThrow<IllegalArgumentException> {
                    service.updateStatus(1L, DeliveryStatus.COMPLETED)
                }
                verify(exactly = 0) { deliveryRepository.save(any()) }
            }
        }
    }
})
