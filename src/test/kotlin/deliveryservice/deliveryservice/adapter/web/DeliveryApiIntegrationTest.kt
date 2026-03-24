package deliveryservice.deliveryservice.adapter.web

import com.ninjasquad.springmockk.MockkBean
import deliveryservice.deliveryservice.adapter.persistence.DeliveryRepository
import deliveryservice.deliveryservice.adapter.persistence.DriverRepository
import deliveryservice.deliveryservice.adapter.redis.RedisDeliveryStatusStore
import deliveryservice.deliveryservice.config.TestRedissonConfig
import deliveryservice.deliveryservice.domain.delivery.Delivery
import deliveryservice.deliveryservice.domain.driver.Driver
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = ["spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}"])
@Import(TestRedissonConfig::class)
class DeliveryApiIntegrationTest {

    @Autowired private lateinit var webApplicationContext: WebApplicationContext
    @Autowired private lateinit var deliveryRepository: DeliveryRepository
    @Autowired private lateinit var driverRepository: DriverRepository

    @MockkBean(relaxed = true) private lateinit var redisDeliveryStatusStore: RedisDeliveryStatusStore

    private lateinit var mockMvc: MockMvc
    private lateinit var driver: Driver
    private lateinit var delivery: Delivery

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
        driver  = driverRepository.save(Driver.create("테스트 기사", 3))
        // DeliveryCreateService를 우회하여 직접 delivery를 생성하므로
        // driver.assignDelivery() 사이드이펙트를 수동으로 반영한다.
        // 이렇게 해야 COMPLETED 시 driver.releaseDelivery()가 정상 동작한다.
        driver.assignDelivery()
        driverRepository.save(driver)
        delivery = deliveryRepository.save(Delivery.create(orderId = 100L, driverId = driver.id!!))
        every { redisDeliveryStatusStore.getStatusCount() } returns emptyMap()
    }

    // ── GET /api/deliveries/{id} ───────────────────────────────────────

    @Test
    fun `배달 조회 - 존재하는 ID - 200 + 상세 정보`() {
        mockMvc.get("/api/deliveries/${delivery.id}").andExpect {
            status { isOk() }
            jsonPath("$.deliveryId") { value(delivery.id!!) }
            jsonPath("$.orderId")    { value(100L) }
            jsonPath("$.driverId")   { value(driver.id!!) }
            jsonPath("$.status")     { value("PENDING") }
        }
    }

    @Test
    fun `배달 조회 - 존재하지 않는 ID - 404`() {
        mockMvc.get("/api/deliveries/99999").andExpect {
            status { isNotFound() }
        }
    }

    // ── PUT /api/deliveries/{id}/status ───────────────────────────────

    @Test
    fun `배달 상태 변경 - PENDING to IN_PROGRESS - 204`() {
        mockMvc.put("/api/deliveries/${delivery.id}/status") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status":"IN_PROGRESS"}"""
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `배달 상태 변경 후 조회 - 상태 반영 확인`() {
        mockMvc.put("/api/deliveries/${delivery.id}/status") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status":"IN_PROGRESS"}"""
        }.andExpect { status { isNoContent() } }

        mockMvc.get("/api/deliveries/${delivery.id}").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("IN_PROGRESS") }
        }
    }

    @Test
    fun `배달 상태 변경 - 존재하지 않는 ID - 404`() {
        mockMvc.put("/api/deliveries/99999/status") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status":"IN_PROGRESS"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `배달 COMPLETED - driver releaseDelivery 반영 확인`() {
        // PENDING → IN_PROGRESS → COMPLETED
        mockMvc.put("/api/deliveries/${delivery.id}/status") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status":"IN_PROGRESS"}"""
        }.andExpect { status { isNoContent() } }

        mockMvc.put("/api/deliveries/${delivery.id}/status") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status":"COMPLETED"}"""
        }.andExpect { status { isNoContent() } }

        // AllMart 문제 8 수정 검증: driver.releaseDelivery() 호출 → currentDeliveryCount 0
        val updatedDriver = driverRepository.findById(driver.id!!).orElseThrow()
        assert(updatedDriver.currentDeliveryCount == 0) {
            "driver.releaseDelivery() 미호출 — currentDeliveryCount: ${updatedDriver.currentDeliveryCount}"
        }
    }

    // ── GET /api/deliveries/status-count ──────────────────────────────

    @Test
    fun `배달 상태 카운트 조회 - 200`() {
        mockMvc.get("/api/deliveries/status-count").andExpect {
            status { isOk() }
        }
    }
}
