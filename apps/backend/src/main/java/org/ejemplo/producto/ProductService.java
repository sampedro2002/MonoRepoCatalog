package org.ejemplo.producto;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.ejemplo.categoria.Categoria;
import org.ejemplo.categoria.ServicioCategoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProductService {

    private final RepositorioProducto repositorioProducto;
    private final ServicioCategoria servicioCategoria;

    public ProductService(RepositorioProducto repositorioProducto, ServicioCategoria servicioCategoria) {
        this.repositorioProducto = repositorioProducto;
        this.servicioCategoria = servicioCategoria;
    }

    @Transactional(readOnly = true)
    public Page<Producto> search(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice cannot be greater than maxPrice");
        }
        return repositorioProducto.findAll(filter(name, categoryId, minPrice, maxPrice), pageable);
    }

    public Producto create(String name, String description, BigDecimal price, Integer stock,
            String imageUrl, Long categoryId) {
        validate(name, description, price, stock, imageUrl);
        Categoria category = servicioCategoria.buscarPorId(categoryId);
        return repositorioProducto.save(
                new Producto(name, description, price, stock, imageUrl, category));
    }

    public Producto update(Long id, String name, String description, BigDecimal price,
            Integer stock, String imageUrl, Long categoryId) {
        validate(name, description, price, stock, imageUrl);
        Producto product = repositorioProducto.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product " + id + " not found"));
        Categoria category = servicioCategoria.buscarPorId(categoryId);
        product.establecerNombre(name);
        product.establecerDescripcion(description);
        product.establecerPrecio(price);
        product.establecerExistencias(stock);
        product.establecerUrlImagen(imageUrl);
        product.establecerCategoria(category);
        return product;
    }

    private Specification<Producto> filter(String name, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(builder.like(builder.lower(root.<String>get("nombre")),
                        "%" + name.toLowerCase() + "%"));
            }
            if (categoryId != null) {
                predicates.add(builder.equal(root.get("categoria").get("id"), categoryId));
            }
            if (minPrice != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.<BigDecimal>get("precio"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(builder.lessThanOrEqualTo(root.<BigDecimal>get("precio"), maxPrice));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validate(String name, String description, BigDecimal price, Integer stock, String imageUrl) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (name.length() > 150) {
            throw new IllegalArgumentException("Product name cannot exceed 150 characters");
        }
        if (description != null && description.length() > 500) {
            throw new IllegalArgumentException("Description cannot exceed 500 characters");
        }
        if (price == null) {
            throw new IllegalArgumentException("Price is required");
        }
        if (price.signum() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (price.scale() > 2 || price.precision() - price.scale() > 10) {
            throw new IllegalArgumentException("Price must have up to 10 integer digits and 2 decimals");
        }
        if (stock == null) {
            throw new IllegalArgumentException("Stock is required");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (imageUrl != null && imageUrl.length() > 500) {
            throw new IllegalArgumentException("Image URL cannot exceed 500 characters");
        }
    }
}
