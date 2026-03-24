package deliveryservice.deliveryservice.adapter.web.dto

import deliveryservice.deliveryservice.domain.driver.Driver
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class AddDriverRequest(
    @field:NotBlank(message = "드라이버 이름은 비어있을 수 없습니다.")
    val name: String,

    @field:Positive(message = "최대 배달 수는 1 이상이어야 합니다.")
    val maxDeliveryCount: Int,
)

data class UpdateDriverMaxCountRequest(
    @field:Positive(message = "최대 배달 수는 1 이상이어야 합니다.")
    val maxDeliveryCount: Int,
)

data class DriverResponse(
    val driverId: Long,
    val name: String,
    val maxDeliveryCount: Int,
    val currentDeliveryCount: Int,
    val canAcceptDelivery: Boolean,
) {
    companion object {
        fun from(driver: Driver) = DriverResponse(
            driverId             = driver.id!!,
            name                 = driver.name,
            maxDeliveryCount     = driver.maxDeliveryCount,
            currentDeliveryCount = driver.currentDeliveryCount,
            canAcceptDelivery    = driver.canAcceptDelivery(),
        )
    }
}