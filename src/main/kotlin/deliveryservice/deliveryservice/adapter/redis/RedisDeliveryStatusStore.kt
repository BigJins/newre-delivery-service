package deliveryservice.deliveryservice.adapter.redis

import deliveryservice.deliveryservice.domain.delivery.DeliveryStatus
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Redis Hash로 실시간 배달 상태 카운트 관리.
 * key: "delivery:status:count", field: "PENDING" / "IN_PROGRESS" / ...
 */
@Component
class RedisDeliveryStatusStore(
    private val redisTemplate: StringRedisTemplate,
) {

    private val hashKey = "delivery:status:count"

    fun increment(status: DeliveryStatus) {
        redisTemplate.opsForHash<String, String>()
            .increment(hashKey, status.name, 1)
    }

    fun decrement(status: DeliveryStatus) {
        redisTemplate.opsForHash<String, String>()
            .increment(hashKey, status.name, -1)
    }

    fun getStatusCount(): Map<DeliveryStatus, Long> {
        val entries = redisTemplate.opsForHash<String, String>().entries(hashKey)
        return entries.mapNotNull { (k, v) ->
            runCatching { DeliveryStatus.valueOf(k) to v.toLong() }.getOrNull()
        }.toMap()
    }
}