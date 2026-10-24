package org.ejemplo.categoria;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServicioCategoria {

    private final RepositorioCategoria repositorioCategoria;

    public ServicioCategoria(RepositorioCategoria repositorioCategoria) {
        this.repositorioCategoria = repositorioCategoria;
    }

    @Transactional(readOnly = true)
    public List<Categoria> buscarTodas() {
        return repositorioCategoria.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return repositorioCategoria.findById(id)
                .orElseThrow(() -> new ExcepcionCategoriaNoEncontrada(id));
    }

    public Categoria crear(String nombre, String descripcion) {
        if (repositorioCategoria.existePorNombre(nombre)) {
            throw new ExcepcionNombreCategoriaDuplicado(nombre);
        }
        return repositorioCategoria.save(new Categoria(nombre, descripcion));
    }

    public Categoria actualizar(Long id, String nombre, String descripcion) {
        Categoria categoria = buscarPorId(id);
        if (!categoria.obtenerNombre().equals(nombre) && repositorioCategoria.existePorNombre(nombre)) {
            throw new ExcepcionNombreCategoriaDuplicado(nombre);
        }
        categoria.establecerNombre(nombre);
        categoria.establecerDescripcion(descripcion);
        return categoria;
    }

    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        repositorioCategoria.delete(categoria);
    }
}
