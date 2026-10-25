-- Producto: pertenece exactamente a una categoria. Una categoria con productos no puede eliminarse (ON DELETE RESTRICT).
CREATE TABLE producto (
    id          BIGSERIAL      PRIMARY KEY,
    nombre        VARCHAR(150)   NOT NULL,
    descripcion VARCHAR(500),
    precio       NUMERIC(12, 2) NOT NULL,
    existencias       INTEGER        NOT NULL,
    url_imagen   VARCHAR(500),
    categoria_id BIGINT         NOT NULL,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id) ON DELETE RESTRICT
);

CREATE INDEX idx_producto_nombre ON producto (nombre);
CREATE INDEX idx_producto_categoria_id ON producto (categoria_id);
