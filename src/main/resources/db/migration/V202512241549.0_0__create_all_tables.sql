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

CREATE TABLE admins
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    telephone  VARCHAR(20)  NOT NULL,
    status     BOOLEAN,
    created_at TIMESTAMP
);

CREATE TABLE employees
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255),
    telephone  VARCHAR(255),
    email      VARCHAR(255),
    password   VARCHAR(255),
    active     BOOLEAN,
    created_at TIMESTAMP,
    admin_id   BIGINT,
    CONSTRAINT fk_employee_admin FOREIGN KEY (admin_id) REFERENCES admins (id)
);

CREATE TABLE fees
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255),
    percentage NUMERIC(19, 2),
    admin_id   BIGINT,
    CONSTRAINT fk_fee_admin FOREIGN KEY (admin_id) REFERENCES admins (id)
);

CREATE TABLE menus
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255),
    description VARCHAR(255),
    price       NUMERIC(19, 2),
    created_at  TIMESTAMP,
    admin_id    BIGINT,
    category_id BIGINT,
    CONSTRAINT fk_menu_admin FOREIGN KEY (admin_id) REFERENCES admins (id),
    CONSTRAINT fk_menu_category FOREIGN KEY (category_id) REFERENCES categorys (id)
);

CREATE TABLE orders
(
    id                 BIGSERIAL PRIMARY KEY,
    table_number       INTEGER      NOT NULL,
    additional_comment VARCHAR(500),
    total_order_price  NUMERIC(10, 2),
    total_fees_value   NUMERIC(10, 2),
    final_total_price  NUMERIC(10, 2),
    status_order       VARCHAR(255) NOT NULL,
    created_at         TIMESTAMP,
    started_at         TIMESTAMP,
    finished_at        TIMESTAMP,
    admin_id           BIGINT       NOT NULL,
    employee_id        BIGINT,
    CONSTRAINT fk_order_admin FOREIGN KEY (admin_id) REFERENCES admins (id),
    CONSTRAINT fk_order_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
);

CREATE TABLE order_items
(
    id       BIGSERIAL PRIMARY KEY,
    quantity INTEGER NOT NULL,
    price    NUMERIC(19, 2),
    menu_id  BIGINT,
    order_id BIGINT,
    CONSTRAINT fk_order_item_menu FOREIGN KEY (menu_id) REFERENCES menus (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE order_fees
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255),
    amount   NUMERIC(19, 2),
    order_id BIGINT,
    CONSTRAINT fk_order_fee_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE TABLE refresh_token
(
    id              BIGSERIAL PRIMARY KEY,
    token           VARCHAR(255) NOT NULL UNIQUE,
    expiration_date TIMESTAMP    NOT NULL,
    admin_id        BIGINT,
    employee_id     BIGINT,
    CONSTRAINT fk_refresh_token_admin FOREIGN KEY (admin_id) REFERENCES admins (id),
    CONSTRAINT fk_refresh_token_employee FOREIGN KEY (employee_id) REFERENCES employees (id)
);

CREATE TABLE admin_profile
(
    admin_id   BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    PRIMARY KEY (admin_id, profile_id),
    CONSTRAINT fk_admin_profile_admin FOREIGN KEY (admin_id) REFERENCES admins (id),
    CONSTRAINT fk_admin_profile_profile FOREIGN KEY (profile_id) REFERENCES profiles (id)
);

CREATE TABLE employee_profile
(
    employee_id BIGINT NOT NULL,
    profile_id  BIGINT NOT NULL,
    PRIMARY KEY (employee_id, profile_id),
    CONSTRAINT fk_employee_profile_employee FOREIGN KEY (employee_id) REFERENCES employees (id),
    CONSTRAINT fk_employee_profile_profile FOREIGN KEY (profile_id) REFERENCES profiles (id)
);