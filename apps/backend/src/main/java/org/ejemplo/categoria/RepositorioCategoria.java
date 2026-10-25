package org.ejemplo.categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositorioCategoria extends JpaRepository<Categoria, Long> {

    @Query("select case when count(c) > 0 then true else false end from Categoria c where c.nombre = :nombre")
    boolean existePorNombre(@Param("nombre") String nombre);
}