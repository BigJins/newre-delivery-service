package deliveryservice.deliveryservice.adapter.persistence

import deliveryservice.deliveryservice.domain.driver.Driver
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository

interface DriverRepository : CrudRepository<Driver, Long> {

    @Query("SELECT * FROM drivers WHERE current_delivery_count < max_delivery_count ORDER BY current_delivery_count ASC")
    fun findAvailable(): List<Driver>
}