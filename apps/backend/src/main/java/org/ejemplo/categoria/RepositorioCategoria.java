package org.ejemplo.categoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioCategoria extends JpaRepository<Categoria, Long> {

    boolean existePorNombre(String nombre);
}
