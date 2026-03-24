package deliveryservice.deliveryservice.domain.delivery

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class DeliveryTest : BehaviorSpec({

    given("Delivery.create()") {
        `when`("유효한 orderId, driverId") {
            then("PENDING 상태로 생성") {
                val delivery = Delivery.create(orderId = 1L, driverId = 10L)
                delivery.orderId shouldBe 1L
                delivery.driverId shouldBe 10L
                delivery.status shouldBe DeliveryStatus.PENDING
                delivery.completedAt shouldBe null
                delivery.cancelledAt shouldBe null
            }
        }
    }

    given("startDelivery()") {
        `when`("PENDING 상태에서") {
            then("IN_PROGRESS로 전환") {
                val delivery = Delivery.create(1L, 10L)
                delivery.startDelivery()
                delivery.status shouldBe DeliveryStatus.IN_PROGRESS
            }
        }
        `when`("COMPLETED 상태에서") {
            then("IllegalArgumentException") {
                val delivery = Delivery.create(1L, 10L)
                delivery.startDelivery()
                delivery.complete()
                shouldThrow<IllegalArgumentException> { delivery.startDelivery() }
            }
        }
    }

    given("complete()") {
        `when`("IN_PROGRESS 상태에서") {
            then("COMPLETED로 전환, completedAt 설정") {
                val delivery = Delivery.create(1L, 10L)
                delivery.startDelivery()
                delivery.complete()
                delivery.status shouldBe DeliveryStatus.COMPLETED
                delivery.completedAt shouldNotBe null
            }
        }
    }

    given("cancel()") {
        `when`("PENDING 상태에서") {
            then("CANCELLED로 전환, cancelledAt 설정") {
                val delivery = Delivery.create(1L, 10L)
                delivery.cancel()
                delivery.status shouldBe DeliveryStatus.CANCELLED
                delivery.cancelledAt shouldNotBe null
            }
        }
        `when`("COMPLETED 상태에서") {
            then("IllegalArgumentException") {
                val delivery = Delivery.create(1L, 10L)
                delivery.startDelivery()
                delivery.complete()
                shouldThrow<IllegalArgumentException> { delivery.cancel() }
            }
        }
    }
})