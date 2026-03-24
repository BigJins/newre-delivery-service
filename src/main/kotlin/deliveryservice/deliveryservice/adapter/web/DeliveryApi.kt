package deliveryservice.deliveryservice.adapter.web

import deliveryservice.deliveryservice.adapter.web.dto.DeliveryResponse
import deliveryservice.deliveryservice.adapter.web.dto.UpdateDeliveryStatusRequest
import deliveryservice.deliveryservice.application.provided.DeliveryFinder
import deliveryservice.deliveryservice.application.provided.DeliveryStatusUpdater
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/deliveries")
class DeliveryApi(
    private val deliveryFinder: DeliveryFinder,
    private val deliveryStatusUpdater: DeliveryStatusUpdater,
) {
    @GetMapping("/{deliveryId}")
    fun getDelivery(@PathVariable deliveryId: Long): ResponseEntity<DeliveryResponse> {
        val delivery = deliveryFinder.findById(deliveryId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(DeliveryResponse.from(delivery))
    }

    @PutMapping("/{deliveryId}/status")
    fun updateStatus(
        @PathVariable deliveryId: Long,
        @Valid @RequestBody request: UpdateDeliveryStatusRequest,
    ): ResponseEntity<Void> {
        deliveryStatusUpdater.updateStatus(deliveryId, request.status)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/status-count")
    fun getStatusCount(): ResponseEntity<Map<String, Long>> {
        val counts = deliveryFinder.getStatusCount().mapKeys { it.key.name }
        return ResponseEntity.ok(counts)
    }
}
