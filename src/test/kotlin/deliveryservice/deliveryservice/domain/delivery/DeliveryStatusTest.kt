package deliveryservice.deliveryservice.domain.delivery

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class DeliveryStatusTest : BehaviorSpec({

    given("PENDING 상태") {
        `when`("IN_PROGRESS로 전환") {
            then("성공") { DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.IN_PROGRESS) shouldBe true }
        }
        `when`("CANCELLED로 전환") {
            then("성공") { DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.CANCELLED) shouldBe true }
        }
        `when`("COMPLETED로 전환") {
            then("불가") { DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.COMPLETED) shouldBe false }
        }
    }

    given("IN_PROGRESS 상태") {
        `when`("COMPLETED로 전환") {
            then("성공") { DeliveryStatus.IN_PROGRESS.canTransitionTo(DeliveryStatus.COMPLETED) shouldBe true }
        }
        `when`("CANCELLED로 전환") {
            then("성공") { DeliveryStatus.IN_PROGRESS.canTransitionTo(DeliveryStatus.CANCELLED) shouldBe true }
        }
        `when`("PENDING으로 전환") {
            then("불가") { DeliveryStatus.IN_PROGRESS.canTransitionTo(DeliveryStatus.PENDING) shouldBe false }
        }
    }

    given("COMPLETED / CANCELLED 상태") {
        then("모든 전환 불가") {
            DeliveryStatus.entries.forEach { next ->
                DeliveryStatus.COMPLETED.canTransitionTo(next) shouldBe false
                DeliveryStatus.CANCELLED.canTransitionTo(next) shouldBe false
            }
        }
    }
})