CREATE TABLE IF NOT EXISTS drivers (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(100) NOT NULL,
    max_delivery_count    INT          NOT NULL,
    current_delivery_count INT         NOT NULL DEFAULT 0,
    INDEX idx_available (current_delivery_count, max_delivery_count)
);

CREATE TABLE IF NOT EXISTS deliveries (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT      NOT NULL,
    driver_id    BIGINT      NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME    NOT NULL,
    completed_at DATETIME,
    cancelled_at DATETIME,
    INDEX idx_order_id (order_id),
    INDEX idx_driver_id (driver_id),
    INDEX idx_status (status)
);

CREATE TABLE IF NOT EXISTS outbox_event (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type     VARCHAR(50)  NOT NULL,
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   VARCHAR(100) NOT NULL,
    payload        LONGTEXT     NOT NULL,
    created_at     DATETIME     NOT NULL,
    INDEX idx_created_at (created_at)
);