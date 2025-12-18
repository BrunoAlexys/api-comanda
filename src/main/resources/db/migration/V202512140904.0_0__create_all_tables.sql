CREATE TABLE profiles
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE categorys
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    telephone  VARCHAR(20)  NOT NULL,
    status     BOOLEAN,
    created_at TIMESTAMP
);

CREATE TABLE user_profile
(
    user_id    BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    CONSTRAINT fk_user_profile_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_profile_profile FOREIGN KEY (profile_id) REFERENCES profiles (id)
);

CREATE TABLE refresh_token
(
    id              BIGSERIAL PRIMARY KEY,
    token           VARCHAR(255) NOT NULL UNIQUE,
    expiration_date TIMESTAMP    NOT NULL,
    user_id         BIGINT,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE fees
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255),
    percentage NUMERIC(19, 2),
    user_id    BIGINT,
    CONSTRAINT fk_fees_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE menus
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(255),
    price       NUMERIC(19, 2),
    created_at  TIMESTAMP,
    user_id     BIGINT,
    category_id BIGINT,
    CONSTRAINT fk_menus_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_menus_category FOREIGN KEY (category_id) REFERENCES categorys (id)
);

CREATE TABLE orders
(
    id                 BIGSERIAL PRIMARY KEY,
    table_number       INTEGER NOT NULL,
    additional_comment VARCHAR(255),
    total_order_price  NUMERIC(19, 2),
    total_fees_value   NUMERIC(19, 2),
    final_total_price  NUMERIC(19, 2),
    created_at         TIMESTAMP
);

CREATE TABLE order_items
(
    id       BIGSERIAL PRIMARY KEY,
    quantity INTEGER NOT NULL,
    price    NUMERIC(19, 2),
    menu_id  BIGINT,
    order_id BIGINT,
    CONSTRAINT fk_order_items_menu FOREIGN KEY (menu_id) REFERENCES menus (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE order_fees
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255),
    amount    NUMERIC(19, 2),
    order_id BIGINT,
    CONSTRAINT fk_order_fees_order FOREIGN KEY (order_id) REFERENCES orders (id)
);