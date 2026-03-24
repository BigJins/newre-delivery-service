package deliveryservice.deliveryservice.application.provided

import deliveryservice.deliveryservice.domain.driver.Driver

/** 인바운드 포트: REST API → 드라이버 관리 */
interface DriverManager {
    fun addDriver(name: String, maxDeliveryCount: Int): Long
    fun updateMaxDeliveryCount(driverId: Long, newMax: Int)
    fun findAll(): List<Driver>
}