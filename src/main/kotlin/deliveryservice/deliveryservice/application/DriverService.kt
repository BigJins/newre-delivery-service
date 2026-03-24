package deliveryservice.deliveryservice.application

import deliveryservice.deliveryservice.adapter.persistence.DriverRepository
import deliveryservice.deliveryservice.application.provided.DriverManager
import deliveryservice.deliveryservice.domain.driver.Driver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DriverService(
    private val driverRepository: DriverRepository,
) : DriverManager {

    override fun addDriver(name: String, maxDeliveryCount: Int): Long {
        val driver = Driver.create(name = name, maxDeliveryCount = maxDeliveryCount)
        return driverRepository.save(driver).id!!
    }

    override fun updateMaxDeliveryCount(driverId: Long, newMax: Int) {
        val driver = driverRepository.findById(driverId).orElse(null)
            ?: throw NoSuchElementException("드라이버를 찾을 수 없습니다: $driverId")
        driver.updateMaxCount(newMax)
        driverRepository.save(driver)
    }

    @Transactional(readOnly = true)
    override fun findAll(): List<Driver> = driverRepository.findAll().toList()
}