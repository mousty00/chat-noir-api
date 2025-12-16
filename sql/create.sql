CREATE TABLE cat_category
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name            VARCHAR(50) NOT NULL UNIQUE,
    media_type_hint VARCHAR(10) NOT NULL -- 'image', 'gif', 'cartoon', 'meme', 'logo'
);


CREATE TABLE cat
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name        VARCHAR(100) NOT NULL,
    color       VARCHAR(100) NOT NULL,
    category_id UUID         NOT NULL,
    source_name VARCHAR(100),

    CONSTRAINT fk_cat_category_entry_uuid
        FOREIGN KEY (category_id)
            REFERENCES cat_category (id)
            ON DELETE CASCADE -- If a category is deleted, entries are deleted
);


CREATE TABLE cat_media_file
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    cat_id UUID         NOT NULL,
    media_format VARCHAR(20)  NOT NULL, -- 'Desktop', 'Phone', 'Original'
    content_url  VARCHAR(512) NOT NULL UNIQUE,
    width_px     INTEGER,
    height_px    INTEGER,

    CONSTRAINT uc_entry_format_uuid UNIQUE (cat_id, media_format),

    CONSTRAINT fk_media_file_entry_uuid
        FOREIGN KEY (cat_id)
            REFERENCES cat (id)
            ON DELETE CASCADE           -- If a cat is deleted, all its media files are deleted
);


CREATE TABLE "user"
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);


CREATE TABLE user_favorite
(
    user_id           UUID NOT NULL,
    cat_media_file_id UUID NOT NULL,

    PRIMARY KEY (user_id, cat_media_file_id),

    CONSTRAINT fk_favorite_user_uuid
        FOREIGN KEY (user_id)
            REFERENCES "user" (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_favorite_cat_media_file_uuid
        FOREIGN KEY (cat_media_file_id)
            REFERENCES cat_media_file (id)
            ON DELETE CASCADE
);