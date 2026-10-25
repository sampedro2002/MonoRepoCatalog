-- Categoria: agrupa productos. Los nombres son unicos, por lo que el servicio puede rechazar duplicados con 409.
CREATE TABLE categoria (
    id          BIGSERIAL    PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
);
