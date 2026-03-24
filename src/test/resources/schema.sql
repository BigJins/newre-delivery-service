CREATE TABLE IF NOT EXISTS drivers (
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                   VARCHAR(100) NOT NULL,
    max_delivery_count     INT          NOT NULL,
    current_delivery_count INT          NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS deliveries (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT      NOT NULL,
    driver_id    BIGINT      NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at   DATETIME    NOT NULL,
    completed_at DATETIME,
    cancelled_at DATETIME
);

CREATE TABLE IF NOT EXISTS outbox_event (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type     VARCHAR(50)  NOT NULL,
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   VARCHAR(100) NOT NULL,
    payload        TEXT         NOT NULL,
    created_at     DATETIME     NOT NULL
);