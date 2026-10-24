-- Category: groups products. Names are unique, so the service can reject duplicates with 409.
CREATE TABLE category (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    CONSTRAINT uk_category_name UNIQUE (name)
);
