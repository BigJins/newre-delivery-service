package deliveryservice.deliveryservice.application

import deliveryservice.deliveryservice.adapter.persistence.DeliveryRepository
import deliveryservice.deliveryservice.adapter.redis.RedisDeliveryStatusStore
import deliveryservice.deliveryservice.application.provided.DeliveryFinder
import deliveryservice.deliveryservice.domain.delivery.Delivery
import deliveryservice.deliveryservice.domain.delivery.DeliveryStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DeliveryQueryService(
    private val deliveryRepository: DeliveryRepository,
    private val statusStore: RedisDeliveryStatusStore,
) : DeliveryFinder {

    override fun findById(id: Long): Delivery? =
        deliveryRepository.findById(id).orElse(null)

    override fun getStatusCount(): Map<DeliveryStatus, Long> =
        statusStore.getStatusCount()
}
