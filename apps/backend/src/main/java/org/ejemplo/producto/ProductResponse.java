package org.ejemplo.producto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        Long categoryId,
        String categoryName) {

    public static ProductResponse from(Producto product) {
        return new ProductResponse(
                product.getId(),
                product.obtenerNombre(),
                product.obtenerDescripcion(),
                product.obtenerPrecio(),
                product.obtenerExistencias(),
                product.obtenerUrlImagen(),
                product.obtenerCategoria().getId(),
                product.obtenerCategoria().obtenerNombre());
    }
}
