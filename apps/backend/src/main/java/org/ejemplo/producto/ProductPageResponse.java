package org.ejemplo.producto;

import java.util.List;
import org.springframework.data.domain.Page;

public record ProductPageResponse(
        List<ProductResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static ProductPageResponse from(Page<Producto> page) {
        return new ProductPageResponse(
                page.map(ProductResponse::from).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
