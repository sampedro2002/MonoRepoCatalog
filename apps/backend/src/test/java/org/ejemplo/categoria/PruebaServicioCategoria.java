package org.ejemplo.categoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServicioCategoriaTest {

    @Mock
    private RepositorioCategoria repositorioCategoria;

    @InjectMocks
    private ServicioCategoria servicioCategoria;

    @Test
    void buscarTodasDevuelveTodasLasCategorias() {
        List<Categoria> categorias = List.of(new Categoria("Bebidas", null), new Categoria("Snacks", null));
        when(repositorioCategoria.findAll()).thenReturn(categorias);

        assertThat(servicioCategoria.buscarTodas()).containsExactlyElementsOf(categorias);
    }

    @Test
    void buscarPorIdDevuelveCategoria() {
        Categoria categoria = new Categoria("Bebidas", "Sin alcohol");
        when(repositorioCategoria.findById(1L)).thenReturn(Optional.of(categoria));

        assertThat(servicioCategoria.buscarPorId(1L)).isSameAs(categoria);
    }

    @Test
    void buscarPorIdLanzaSiFalta() {
        when(repositorioCategoria.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioCategoria.buscarPorId(99L))
                .isInstanceOf(ExcepcionCategoriaNoEncontrada.class);
    }

    @Test
    void crearGuardaCategoriaCuandoNombreEstaDisponible() {
        when(repositorioCategoria.existePorNombre("Bebidas")).thenReturn(false);
        when(repositorioCategoria.save(any(Categoria.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Categoria creada = servicioCategoria.crear("Bebidas", "Sin alcohol");

        assertThat(creada.obtenerNombre()).isEqualTo("Bebidas");
        assertThat(creada.obtenerDescripcion()).isEqualTo("Sin alcohol");
        verify(repositorioCategoria).save(creada);
    }

    @Test
    void crearLanzaCuandoNombreYaExiste() {
        when(repositorioCategoria.existePorNombre("Bebidas")).thenReturn(true);

        assertThatThrownBy(() -> servicioCategoria.crear("Bebidas", null))
                .isInstanceOf(ExcepcionNombreCategoriaDuplicado.class);
        verify(repositorioCategoria, never()).save(any());
    }

    @Test
    void actualizarCambiaCamposCuandoNombreNoCambia() {
        Categoria categoria = new Categoria("Bebidas", "Viejo");
        when(repositorioCategoria.findById(1L)).thenReturn(Optional.of(categoria));

        Categoria actualizada = servicioCategoria.actualizar(1L, "Bebidas", "Nuevo");

        assertThat(actualizada.obtenerDescripcion()).isEqualTo("Nuevo");
        verify(repositorioCategoria, never()).existePorNombre(any());
    }

    @Test
    void actualizarRenombresCuandoNuevoNombreEstaDisponible() {
        Categoria categoria = new Categoria("Bebidas", null);
        when(repositorioCategoria.findById(1L)).thenReturn(Optional.of(categoria));
        when(repositorioCategoria.existePorNombre("Jugos")).thenReturn(false);

        Categoria actualizada = servicioCategoria.actualizar(1L, "Jugos", null);

        assertThat(actualizada.obtenerNombre()).isEqualTo("Jugos");
    }

    @Test
    void actualizarLanzaAlRenombrarAUnNombreExistente() {
        Categoria categoria = new Categoria("Bebidas", null);
        when(repositorioCategoria.findById(1L)).thenReturn(Optional.of(categoria));
        when(repositorioCategoria.existePorNombre("Snacks")).thenReturn(true);

        assertThatThrownBy(() -> servicioCategoria.actualizar(1L, "Snacks", null))
                .isInstanceOf(ExcepcionNombreCategoriaDuplicado.class);
        assertThat(categoria.obtenerNombre()).isEqualTo("Bebidas");
    }

    @Test
    void actualizarLanzaSiFalta() {
        when(repositorioCategoria.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioCategoria.actualizar(99L, "Bebidas", null))
                .isInstanceOf(ExcepcionCategoriaNoEncontrada.class);
    }

    @Test
    void eliminarEliminaCategoriaExistente() {
        Categoria categoria = new Categoria("Bebidas", null);
        when(repositorioCategoria.findById(1L)).thenReturn(Optional.of(categoria));

        servicioCategoria.eliminar(1L);

        verify(repositorioCategoria).delete(categoria);
    }

    @Test
    void eliminarLanzaSiFalta() {
        when(repositorioCategoria.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioCategoria.eliminar(99L))
                .isInstanceOf(ExcepcionCategoriaNoEncontrada.class);
        verify(repositorioCategoria, never()).delete(any());
    }
}
