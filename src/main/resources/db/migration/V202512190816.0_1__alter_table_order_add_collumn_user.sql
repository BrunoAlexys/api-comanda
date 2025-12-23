ALTER TABLE orders
    ADD COLUMN user_id BIGINT NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id);