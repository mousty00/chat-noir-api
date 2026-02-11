
CREATE TABLE cat_category
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(50) NOT NULL UNIQUE,
    media_type_hint VARCHAR(10) NOT NULL
);

CREATE TABLE cat
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(25) NOT NULL,
    color       VARCHAR(25),
    category_id UUID,
    source_name VARCHAR(50), -- creator

    CONSTRAINT fk_cat_category
        FOREIGN KEY (category_id)
            REFERENCES cat_category (id)
            ON DELETE CASCADE
);

CREATE TABLE cat_media
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cat_id       UUID         NOT NULL,
    media_format VARCHAR(20)  NOT NULL, -- gif, png, jpeg
    content_url  VARCHAR(512) NOT NULL UNIQUE,

    CONSTRAINT uc_cat_format UNIQUE (cat_id, media_format),

    CONSTRAINT fk_cat_media_cat
        FOREIGN KEY (cat_id)
            REFERENCES cat (id)
            ON DELETE CASCADE
);

CREATE TABLE user_role
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(25) NOT NULL UNIQUE,
    description VARCHAR(100)
);

CREATE TABLE subscription_plan
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(50)    NOT NULL UNIQUE,  -- "Free", "Pro", "Enterprise"
    monthly_price       NUMERIC(10, 2) NOT NULL,
    yearly_price        NUMERIC(10, 2) NOT NULL,
    api_rate_limit      INTEGER        NOT NULL DEFAULT 0, -- max requests per hour
    max_favorites       INTEGER        NOT NULL DEFAULT 20,
    access_to_gif       BOOLEAN        NOT NULL DEFAULT TRUE,
    access_to_memes     BOOLEAN        NOT NULL DEFAULT TRUE,
    access_to_wallpaper BOOLEAN        NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN        NOT NULL DEFAULT TRUE, -- Flag to enable/disable plans
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_media
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID         NOT NULL,
    media_format   VARCHAR(50)  NOT NULL, -- gif, png, jpeg
    url  VARCHAR(512) NOT NULL,
    uploaded_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    width_px     INTEGER,
    height_px    INTEGER
);

CREATE TABLE "user"
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    role_id       UUID         NOT NULL,
    is_admin      BOOLEAN      NOT NULL,

    plan_id       UUID,
    subscription_start_date TIMESTAMP,
    subscription_end_date   TIMESTAMP,
    stripe_subscription_id VARCHAR(255),

    profile_media_id UUID,

    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id)
            REFERENCES user_role (id),

    CONSTRAINT fk_user_plan
        FOREIGN KEY (plan_id)
            REFERENCES subscription_plan (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_user_profile_media
        FOREIGN KEY (profile_media_id)
            REFERENCES user_media (id)
            ON DELETE SET NULL
);

ALTER TABLE user_media
    ADD CONSTRAINT fk_user_media_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (id)
            ON DELETE CASCADE;

CREATE TABLE user_api_key
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL,
    api_key     VARCHAR(128) NOT NULL UNIQUE, -- hashed api key
    name        VARCHAR(100),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  TIMESTAMP,
    is_revoked  BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_api_key_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (id)
            ON DELETE CASCADE
);

CREATE TABLE user_favorite
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID NOT NULL,
    cat_media_id      UUID NOT NULL,

    CONSTRAINT fk_favorite_user
        FOREIGN KEY (user_id)
            REFERENCES "user" (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_favorite_cat_media
        FOREIGN KEY (cat_media_id)
            REFERENCES cat_media (id)
            ON DELETE CASCADE
);
