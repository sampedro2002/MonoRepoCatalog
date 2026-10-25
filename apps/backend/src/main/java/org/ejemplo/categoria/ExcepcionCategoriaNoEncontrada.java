package org.ejemplo.categoria;

public class ExcepcionCategoriaNoEncontrada extends RuntimeException {

    public ExcepcionCategoriaNoEncontrada(Long id) {
        super("Categoria " + id + " no encontrada");
    }
}
