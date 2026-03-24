package deliveryservice.deliveryservice.application.provided

import deliveryservice.deliveryservice.domain.delivery.Delivery
import deliveryservice.deliveryservice.domain.delivery.DeliveryStatus

interface DeliveryFinder {
    fun findById(id: Long): Delivery?
    fun getStatusCount(): Map<DeliveryStatus, Long>
}
