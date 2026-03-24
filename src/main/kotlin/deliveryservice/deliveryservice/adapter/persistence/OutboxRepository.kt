package deliveryservice.deliveryservice.adapter.persistence

import deliveryservice.deliveryservice.domain.outbox.OutboxEvent
import org.springframework.data.repository.CrudRepository

interface OutboxRepository : CrudRepository<OutboxEvent, Long>