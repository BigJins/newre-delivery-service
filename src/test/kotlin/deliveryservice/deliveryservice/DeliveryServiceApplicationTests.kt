package deliveryservice.deliveryservice

import deliveryservice.deliveryservice.config.TestRedissonConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = ["spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}"])
@Import(TestRedissonConfig::class)
class DeliveryServiceApplicationTests {

    @Test
    fun contextLoads() {
    }
}
