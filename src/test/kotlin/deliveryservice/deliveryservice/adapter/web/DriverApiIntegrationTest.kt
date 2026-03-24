package deliveryservice.deliveryservice.adapter.web

import com.ninjasquad.springmockk.MockkBean
import deliveryservice.deliveryservice.adapter.redis.RedisDeliveryStatusStore
import deliveryservice.deliveryservice.config.TestRedissonConfig
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
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1)
@TestPropertySource(properties = ["spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}"])
@Import(TestRedissonConfig::class)
class DriverApiIntegrationTest {

    @Autowired private lateinit var webApplicationContext: WebApplicationContext

    @MockkBean(relaxed = true) private lateinit var redisDeliveryStatusStore: RedisDeliveryStatusStore

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build()
    }

    // ── POST /api/drivers ──────────────────────────────────────────────

    @Test
    fun `드라이버 등록 - 유효한 요청 - 201 + driverId 반환`() {
        mockMvc.post("/api/drivers") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"홍길동","maxDeliveryCount":3}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.driverId") { exists() }
        }
    }

    @Test
    fun `드라이버 등록 - 이름 빈 문자열 - 400`() {
        mockMvc.post("/api/drivers") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"","maxDeliveryCount":3}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `드라이버 등록 - maxDeliveryCount 0 - 400`() {
        mockMvc.post("/api/drivers") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"홍길동","maxDeliveryCount":0}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // ── GET /api/drivers ───────────────────────────────────────────────

    @Test
    fun `드라이버 전체 조회 - 200 + 배열 반환`() {
        mockMvc.post("/api/drivers") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"김철수","maxDeliveryCount":2}"""
        }.andExpect { status { isCreated() } }

        mockMvc.get("/api/drivers").andExpect {
            status { isOk() }
            jsonPath("$") { isArray() }
            jsonPath("$[0].name") { value("김철수") }
            jsonPath("$[0].canAcceptDelivery") { value(true) }
        }
    }

    // ── PATCH /api/drivers/{id}/max-count ─────────────────────────────

    @Test
    fun `최대 배달 수 변경 - 204`() {
        val result = mockMvc.post("/api/drivers") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"이영희","maxDeliveryCount":2}"""
        }.andExpect { status { isCreated() } }
            .andReturn()

        val driverId = result.response.getHeader("Location")!!.substringAfterLast("/")

        mockMvc.patch("/api/drivers/$driverId/max-count") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"maxDeliveryCount":5}"""
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `최대 배달 수 변경 - 존재하지 않는 드라이버 - 404`() {
        mockMvc.patch("/api/drivers/99999/max-count") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"maxDeliveryCount":5}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `최대 배달 수 변경 - maxDeliveryCount 0 - 400`() {
        mockMvc.patch("/api/drivers/1/max-count") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"maxDeliveryCount":0}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }
}
