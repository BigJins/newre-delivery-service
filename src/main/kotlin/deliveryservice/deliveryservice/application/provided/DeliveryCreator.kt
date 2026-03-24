package deliveryservice.deliveryservice.application.provided

/** 인바운드 포트: Kafka Consumer → 배달 생성 */
interface DeliveryCreator {
    fun create(orderId: Long): Long
}