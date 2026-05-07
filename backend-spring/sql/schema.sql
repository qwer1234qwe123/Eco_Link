#테이블 6개 생성 sql 작성
CREATE TABLE trash_can (
    id bigint NOT NULL AUTO_INCREMENT,
    loc_name varchar(100) NOT NULL,
    loc_lat double NOT NULL,
    loc_lng double NOT NULL,
    max_capa int NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE worker (
    id bigint NOT NULL AUTO_INCREMENT,
    username varchar(50) NOT NULL,
    password varchar(255) NOT NULL,
    vehicle_number varchar(20) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_worker_username (username)
);

CREATE TABLE sensor_log (
    id bigint NOT NULL AUTO_INCREMENT,
    can_id bigint NOT NULL,
    fill_level int NOT NULL,
    battery_level int NOT NULL,
    log_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_sensor_log_can_id (can_id),
    KEY idx_sensor_log_log_time (log_time),
    CONSTRAINT fk_sensor_log_can
        FOREIGN KEY (can_id) REFERENCES trash_can (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE collection_route (
    id bigint NOT NULL AUTO_INCREMENT,
    worker_id bigint NOT NULL,
    optimized_path json NOT NULL,
    total_distance double NOT NULL DEFAULT 0,
    created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_collection_route_worker_id (worker_id),
    CONSTRAINT fk_collection_route_worker
        FOREIGN KEY (worker_id) REFERENCES worker (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE collection_history (
    id bigint NOT NULL AUTO_INCREMENT,
    route_id bigint NOT NULL,
    can_id bigint NOT NULL,
    before_level int NOT NULL,
    after_level int NOT NULL,
    collected_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_collection_history_route_id (route_id),
    KEY idx_collection_history_can_id (can_id),
    CONSTRAINT fk_collection_history_route
        FOREIGN KEY (route_id) REFERENCES collection_route (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_collection_history_can
        FOREIGN KEY (can_id) REFERENCES trash_can (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE can_status_log (
    id bigint NOT NULL AUTO_INCREMENT,
    can_id bigint NOT NULL,
    prev_status varchar(50) NULL,
    curr_status varchar(50) NOT NULL,
    reason text NULL,
    changed_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_can_status_log_can_id (can_id),
    KEY idx_can_status_log_changed_at (changed_at),
    CONSTRAINT fk_can_status_log_can
        FOREIGN KEY (can_id) REFERENCES trash_can (id)
        ON DELETE CASCADE ON UPDATE CASCADE
);