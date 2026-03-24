package deliveryservice.deliveryservice.domain.delivery

/**
 * AllMart 문제: START 상태가 즉시 IN_PROGRESS로 덮어써짐 → START 제거, PENDING → IN_PROGRESS로 직접 전환.
 * exhaustive when → 새 상태 추가 시 컴파일 에러로 누락 방지.
 */
enum class DeliveryStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED;

    fun canTransitionTo(next: DeliveryStatus): Boolean = when (this) {
        PENDING     -> next == IN_PROGRESS || next == CANCELLED
        IN_PROGRESS -> next == COMPLETED || next == CANCELLED
        COMPLETED   -> false
        CANCELLED   -> false
    }
}