package deliveryservice.deliveryservice.config

import io.mockk.mockk
import org.redisson.api.RedissonClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

/**
 * 테스트 전용 RedissonClient mock bean.
 *
 * redisson-spring-boot-starter:3.44.0 은 Spring Boot 4 에서
 * 이동된 org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration 을 참조해
 * ClassNotFoundException 을 발생시킨다.
 *
 * application-test.yaml 에서 RedissonAutoConfiguration 을 exclude 하고
 * 이 설정 클래스로 대체하여 DeliveryCreateService 의 의존성을 만족시킨다.
 */
@TestConfiguration
class TestRedissonConfig {
    @Bean
    @Primary
    fun redissonClient(): RedissonClient = mockk(relaxed = true)
}
