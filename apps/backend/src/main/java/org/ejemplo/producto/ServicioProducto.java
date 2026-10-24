package org.ejemplo.producto;

import java.math.BigDecimal;
import org.ejemplo.categoria.Categoria;
import org.ejemplo.categoria.ServicioCategoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioProducto {

    private final RepositorioProducto repositorioProducto;
    private final ServicioCategoria servicioCategoria;

    public ServicioProducto(RepositorioProducto repositorioProducto, ServicioCategoria servicioCategoria) {
        this.repositorioProducto = repositorioProducto;
        this.servicioCategoria = servicioCategoria;
    }

    public Producto crear(String nombre, String descripcion, BigDecimal precio, Integer existencias,
            String urlImagen, Long categoriaId) {
        validar(nombre, descripcion, precio, existencias, urlImagen);
        Categoria categoria = servicioCategoria.buscarPorId(categoriaId);
        return repositorioProducto.save(
                new Producto(nombre, descripcion, precio, existencias, urlImagen, categoria));
    }

    public Producto actualizar(Long id, String nombre, String descripcion, BigDecimal precio,
            Integer existencias, String urlImagen, Long categoriaId) {
        validar(nombre, descripcion, precio, existencias, urlImagen);
        Producto producto = repositorioProducto.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto " + id + " no encontrado"));
        Categoria categoria = servicioCategoria.buscarPorId(categoriaId);
        producto.establecerNombre(nombre);
        producto.establecerDescripcion(descripcion);
        producto.establecerPrecio(precio);
        producto.establecerExistencias(existencias);
        producto.establecerUrlImagen(urlImagen);
        producto.establecerCategoria(categoria);
        return producto;
    }

    private void validar(String nombre, String descripcion, BigDecimal precio, Integer existencias,
            String urlImagen) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (nombre.length() > 150) {
            throw new IllegalArgumentException("El nombre del producto no puede superar 150 caracteres");
        }
        if (descripcion != null && descripcion.length() > 500) {
            throw new IllegalArgumentException("La descripcion no puede superar 500 caracteres");
        }
        if (precio == null) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }
        if (precio.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (precio.scale() > 2 || precio.precision() - precio.scale() > 10) {
            throw new IllegalArgumentException("El precio debe tener hasta 10 digitos enteros y 2 decimales");
        }
        if (existencias == null) {
            throw new IllegalArgumentException("Las existencias son obligatorias");
        }
        if (existencias < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas");
        }
        if (urlImagen != null && urlImagen.length() > 500) {
            throw new IllegalArgumentException("La URL de la imagen no puede superar 500 caracteres");
        }
    }
}
