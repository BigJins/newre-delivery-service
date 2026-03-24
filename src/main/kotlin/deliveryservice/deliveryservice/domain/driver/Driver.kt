package deliveryservice.deliveryservice.domain.driver

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator
import org.springframework.data.relational.core.mapping.Table

/**
 * Driver Aggregate Root.
 *
 * AllMart 문제 수정:
 * - assignDelivery()만 있고 completeDelivery() 호출 없음 → currentDeliveryCount 영원히 증가
 * - 재설계: completeDelivery() / cancelDelivery() 반드시 호출하여 카운트 감소 보장
 *
 * Spring Data JDBC: @PersistenceCreator → private constructor 허용
 */
@Table("drivers")
class Driver @PersistenceCreator private constructor(
    @Id val id: Long? = null,
    val name: String,
    var maxDeliveryCount: Int,
    var currentDeliveryCount: Int = 0,
) {
    companion object {
        fun create(name: String, maxDeliveryCount: Int): Driver {
            require(name.isNotBlank()) { "드라이버 이름은 비어있을 수 없습니다." }
            require(maxDeliveryCount > 0) { "최대 배달 수는 1 이상이어야 합니다." }
            return Driver(name = name, maxDeliveryCount = maxDeliveryCount)
        }
    }

    fun canAcceptDelivery(): Boolean = currentDeliveryCount < maxDeliveryCount

    /** 배달 배정: currentDeliveryCount 증가 */
    fun assignDelivery() {
        check(canAcceptDelivery()) { "드라이버 ${id}의 배달 용량이 초과되었습니다." }
        currentDeliveryCount++
    }

    /** 배달 완료/취소: currentDeliveryCount 감소 (AllMart 누락 버그 수정) */
    fun releaseDelivery() {
        check(currentDeliveryCount > 0) { "드라이버 ${id}의 배달 카운트가 이미 0입니다." }
        currentDeliveryCount--
    }

    /** 최대 배달 수 변경 (currentDeliveryCount보다 작아질 수 없음) */
    fun updateMaxCount(newMax: Int) {
        require(newMax > 0) { "최대 배달 수는 1 이상이어야 합니다." }
        require(newMax >= currentDeliveryCount) {
            "최대 배달 수($newMax)는 현재 배달 수($currentDeliveryCount)보다 작을 수 없습니다."
        }
        maxDeliveryCount = newMax
    }
}