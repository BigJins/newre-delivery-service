package deliveryservice.deliveryservice.domain.driver

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class DriverTest : BehaviorSpec({

    given("Driver.create()") {
        `when`("유효한 이름과 maxDeliveryCount") {
            then("정상 생성") {
                val driver = Driver.create("홍길동", 3)
                driver.name shouldBe "홍길동"
                driver.maxDeliveryCount shouldBe 3
                driver.currentDeliveryCount shouldBe 0
                driver.canAcceptDelivery() shouldBe true
            }
        }
        `when`("이름이 빈 문자열") {
            then("IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> { Driver.create("", 3) }
            }
        }
        `when`("maxDeliveryCount가 0") {
            then("IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> { Driver.create("홍길동", 0) }
            }
        }
    }

    given("assignDelivery()") {
        `when`("용량 여유 있을 때") {
            then("currentDeliveryCount 증가") {
                val driver = Driver.create("홍길동", 2)
                driver.assignDelivery()
                driver.currentDeliveryCount shouldBe 1
                driver.canAcceptDelivery() shouldBe true
            }
        }
        `when`("최대 용량 도달 후 추가 배정") {
            then("IllegalStateException") {
                val driver = Driver.create("홍길동", 1)
                driver.assignDelivery()
                shouldThrow<IllegalStateException> { driver.assignDelivery() }
            }
        }
    }

    given("updateMaxCount()") {
        `when`("현재 배달 수보다 큰 값으로 변경") {
            then("maxDeliveryCount 갱신") {
                val driver = Driver.create("홍길동", 3)
                driver.updateMaxCount(5)
                driver.maxDeliveryCount shouldBe 5
            }
        }
        `when`("0 이하 값으로 변경 시도") {
            then("IllegalArgumentException") {
                val driver = Driver.create("홍길동", 3)
                shouldThrow<IllegalArgumentException> { driver.updateMaxCount(0) }
            }
        }
        `when`("현재 배달 수보다 작은 값으로 변경 시도") {
            then("IllegalArgumentException") {
                val driver = Driver.create("홍길동", 3)
                driver.assignDelivery()
                driver.assignDelivery()
                shouldThrow<IllegalArgumentException> { driver.updateMaxCount(1) }
            }
        }
    }

    given("releaseDelivery() — AllMart 누락 버그 수정") {
        `when`("배달 완료 후 카운트 감소") {
            then("currentDeliveryCount 감소") {
                val driver = Driver.create("홍길동", 2)
                driver.assignDelivery()
                driver.assignDelivery()
                driver.currentDeliveryCount shouldBe 2
                driver.canAcceptDelivery() shouldBe false

                driver.releaseDelivery()
                driver.currentDeliveryCount shouldBe 1
                driver.canAcceptDelivery() shouldBe true
            }
        }
        `when`("카운트가 0인데 release 시도") {
            then("IllegalStateException") {
                val driver = Driver.create("홍길동", 2)
                shouldThrow<IllegalStateException> { driver.releaseDelivery() }
            }
        }
    }
})