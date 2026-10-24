package org.ejemplo.categoria;

public record RespuestaCategoria(Long id, String nombre, String descripcion) {

    public static RespuestaCategoria desde(Categoria categoria) {
        return new RespuestaCategoria(categoria.getId(), categoria.obtenerNombre(), categoria.obtenerDescripcion());
    }
}
