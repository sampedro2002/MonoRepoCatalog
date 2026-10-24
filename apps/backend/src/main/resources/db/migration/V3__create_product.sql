-- Product: belongs to exactly one category. A category with products cannot be deleted (ON DELETE RESTRICT).
CREATE TABLE product (
    id          BIGSERIAL      PRIMARY KEY,
    name        VARCHAR(150)   NOT NULL,
    description VARCHAR(500),
    price       NUMERIC(12, 2) NOT NULL,
    stock       INTEGER        NOT NULL,
    image_url   VARCHAR(500),
    category_id BIGINT         NOT NULL,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id) ON DELETE RESTRICT
);

CREATE INDEX idx_product_name ON product (name);
CREATE INDEX idx_product_category_id ON product (category_id);
