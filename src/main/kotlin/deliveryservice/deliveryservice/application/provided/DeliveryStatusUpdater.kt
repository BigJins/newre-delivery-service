package deliveryservice.deliveryservice.application.provided

import deliveryservice.deliveryservice.domain.delivery.DeliveryStatus

/** 인바운드 포트: REST API → 배달 상태 변경 */
interface DeliveryStatusUpdater {
    fun updateStatus(deliveryId: Long, newStatus: DeliveryStatus)
}