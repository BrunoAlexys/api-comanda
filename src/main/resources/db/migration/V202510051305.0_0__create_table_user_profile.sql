CREATE TABLE user_profile
(
    user_id    BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, profile_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_profile FOREIGN KEY (profile_id) REFERENCES profiles (id)
);