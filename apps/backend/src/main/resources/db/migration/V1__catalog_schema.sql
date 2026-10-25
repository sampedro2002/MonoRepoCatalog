-- Initial schema: the product catalog.
-- Seed data lives in R__seed_products.sql, which Flyway runs after the versioned migrations.

CREATE TABLE products (
    id          BIGSERIAL      PRIMARY KEY,
    name        VARCHAR(120)   NOT NULL,
    description VARCHAR(500),
    price       NUMERIC(19, 2) NOT NULL,
    stock       INTEGER        NOT NULL,
    image_url   VARCHAR(500)
);
