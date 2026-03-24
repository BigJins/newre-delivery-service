package deliveryservice.deliveryservice.adapter.web

import deliveryservice.deliveryservice.adapter.web.dto.AddDriverRequest
import deliveryservice.deliveryservice.adapter.web.dto.DriverResponse
import deliveryservice.deliveryservice.adapter.web.dto.UpdateDriverMaxCountRequest
import deliveryservice.deliveryservice.application.provided.DriverManager
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/drivers")
class DriverApi(
    private val driverManager: DriverManager,
) {
    @PostMapping
    fun addDriver(@Valid @RequestBody request: AddDriverRequest): ResponseEntity<Map<String, Long>> {
        val driverId = driverManager.addDriver(request.name, request.maxDeliveryCount)
        return ResponseEntity
            .created(URI.create("/api/drivers/$driverId"))
            .body(mapOf("driverId" to driverId))
    }

    @GetMapping
    fun getAllDrivers(): ResponseEntity<List<DriverResponse>> {
        val drivers = driverManager.findAll().map { DriverResponse.from(it) }
        return ResponseEntity.ok(drivers)
    }

    @PatchMapping("/{driverId}/max-count")
    fun updateMaxCount(
        @PathVariable driverId: Long,
        @Valid @RequestBody request: UpdateDriverMaxCountRequest,
    ): ResponseEntity<Void> {
        driverManager.updateMaxDeliveryCount(driverId, request.maxDeliveryCount)
        return ResponseEntity.noContent().build()
    }
}