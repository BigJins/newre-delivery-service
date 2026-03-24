package deliveryservice.deliveryservice.adapter.persistence

import deliveryservice.deliveryservice.domain.delivery.Delivery
import org.springframework.data.repository.CrudRepository

interface DeliveryRepository : CrudRepository<Delivery, Long>