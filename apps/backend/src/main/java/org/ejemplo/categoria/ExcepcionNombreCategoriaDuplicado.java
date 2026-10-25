package org.ejemplo.categoria;

public class ExcepcionNombreCategoriaDuplicado extends RuntimeException {

    public ExcepcionNombreCategoriaDuplicado(String nombre) {
        super("El nombre de la categoria ya existe: " + nombre);
    }
}
