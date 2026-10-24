package org.ejemplo.producto;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositorioProducto extends JpaRepository<Producto, Long> {

    @Query("select p from Producto p where p.categoria.id = :categoriaId")
    Page<Producto> buscarPorCategoriaId(@Param("categoriaId") Long categoriaId, Pageable pageable);

    @Query("""
            select p from Producto p
            where p.categoria.id = :categoriaId
              and lower(p.nombre) like lower(concat('%', :nombre, '%'))
            """)
    Page<Producto> buscarPorCategoriaIdYNombreContieneIgnorandoMayusculas(
            @Param("categoriaId") Long categoriaId, @Param("nombre") String nombre, Pageable pageable);
}
