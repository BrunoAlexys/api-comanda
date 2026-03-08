ALTER TABLE categorys
    ADD COLUMN admin_id BIGINT;

ALTER TABLE categorys
    ADD CONSTRAINT fk_category_admin
        FOREIGN KEY (admin_id)
            REFERENCES admins (id);