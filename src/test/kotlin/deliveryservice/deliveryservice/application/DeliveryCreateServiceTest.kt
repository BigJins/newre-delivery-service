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
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import tools.jackson.databind.ObjectMapper
import java.util.Optional

class DeliveryCreateServiceTest : BehaviorSpec({

    val driverRepository   = mockk<DriverRepository>()
    val deliveryRepository = mockk<DeliveryRepository>()
    val outboxRepository   = mockk<OutboxRepository>()
    val statusStore        = mockk<RedisDeliveryStatusStore>()
    val redissonClient     = mockk<RedissonClient>()
    val objectMapper       = mockk<ObjectMapper>()

    val service = DeliveryCreateService(
        driverRepository, deliveryRepository, outboxRepository,
        statusStore, redissonClient, objectMapper
    )

    afterEach { clearAllMocks() }

    given("배정 가능한 드라이버가 있을 때") {
        // Driver.create()는 id=null 로 생성 → service의 driver.id!! 에서 NPE 발생
        // mockk 으로 id=1L 을 반환하는 드라이버를 제공한다.
        val driver = mockk<Driver> {
            every { id } returns 1L
            every { canAcceptDelivery() } returns true
            every { assignDelivery() } just runs
        }
        val mockLock       = mockk<RLock>()
        val savedDelivery  = mockk<Delivery>()

        beforeEach {
            every { driverRepository.findAvailable() }      returns listOf(driver)
            every { redissonClient.getLock(any<String>()) } returns mockLock
            every { mockLock.tryLock() }                    returns true
            every { mockLock.isHeldByCurrentThread }        returns true
            every { mockLock.unlock() }                     returns Unit
            every { driverRepository.findById(1L) }         returns Optional.of(driver)
            every { driverRepository.save(any()) }          returns driver
            every { savedDelivery.id }                      returns 1L
            every { savedDelivery.orderId }                 returns 100L
            every { savedDelivery.driverId }                returns 1L
            every { savedDelivery.status }                  returns DeliveryStatus.PENDING
            every { deliveryRepository.save(any()) }        returns savedDelivery
            every { objectMapper.writeValueAsString(any()) } returns """{"deliveryId":1}"""
            every { outboxRepository.save(any()) }          returnsArgument 0
            every { statusStore.increment(any()) }          returns Unit
        }

        `when`("create() 호출") {
            then("Delivery, Outbox 각 1회 저장, deliveryId 반환") {
                val result = service.create(100L)
                result shouldBe 1L
                verify(exactly = 1) { deliveryRepository.save(any()) }
                verify(exactly = 1) { outboxRepository.save(any()) }
                verify(exactly = 1) { statusStore.increment(DeliveryStatus.PENDING) }
            }
        }
    }

    given("배정 가능한 드라이버가 없을 때") {
        `when`("create() 호출") {
            then("IllegalStateException, Repository 미호출") {
                every { driverRepository.findAvailable() } returns emptyList()
                shouldThrow<IllegalStateException> { service.create(100L) }
                verify(exactly = 0) { deliveryRepository.save(any()) }
                verify(exactly = 0) { outboxRepository.save(any()) }
            }
        }
    }
})
