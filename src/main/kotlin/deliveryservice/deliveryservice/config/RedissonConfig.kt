package deliveryservice.deliveryservice.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Redisson 클라이언트 설정.
 *
 * redisson-spring-boot-starter:3.44.0 은 Spring Boot 4 와 비호환 (구 RedisAutoConfiguration 참조).
 * 스타터 대신 redisson 라이브러리를 직접 사용하고 여기서 빈을 등록한다.
 *
 * @ConditionalOnMissingBean: 테스트에서 TestRedissonConfig 가 mock 빈을 먼저 등록하면
 *                            이 메서드는 실행되지 않아 실제 Redis 연결을 시도하지 않는다.
 */
@Configuration
class RedissonConfig(
    @Value("\${spring.data.redis.host}") private val host: String,
    @Value("\${spring.data.redis.port}") private val port: Int,
) {
    @Bean
    @ConditionalOnMissingBean
    fun redissonClient(): RedissonClient {
        val config = Config()
        config.useSingleServer().setAddress("redis://$host:$port")
        return Redisson.create(config)
    }
}
