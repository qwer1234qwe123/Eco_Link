ALTER TABLE collection_route
DROP FOREIGN KEY fk_collection_route_worker,
DROP INDEX idx_collection_route_worker_id,
CHANGE worker_id user_id BIGINT NOT NULL,
ADD INDEX idx_collection_route_user_id (user_id),
ADD CONSTRAINT fk_collection_route_user 
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE;