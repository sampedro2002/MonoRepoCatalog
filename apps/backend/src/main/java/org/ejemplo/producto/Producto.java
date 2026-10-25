package org.ejemplo.producto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.ejemplo.categoria.Categoria;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "existencias", nullable = false)
    private Integer existencias;

    @Column(name = "url_imagen", length = 500)
    private String urlImagen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_producto_categoria"))
    private Categoria categoria;

    protected Producto() {
    }

    public Producto(String nombre, String descripcion, BigDecimal precio, Integer existencias,
            String urlImagen, Categoria categoria) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.existencias = existencias;
        this.urlImagen = urlImagen;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public String obtenerNombre() {
        return nombre;
    }

    public void establecerNombre(String nombre) {
        this.nombre = nombre;
    }

    public String obtenerDescripcion() {
        return descripcion;
    }

    public void establecerDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal obtenerPrecio() {
        return precio;
    }

    public void establecerPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer obtenerExistencias() {
        return existencias;
    }

    public void establecerExistencias(Integer existencias) {
        this.existencias = existencias;
    }

    public String obtenerUrlImagen() {
        return urlImagen;
    }

    public void establecerUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public Categoria obtenerCategoria() {
        return categoria;
    }

    public void establecerCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}
