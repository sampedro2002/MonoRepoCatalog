package org.ejemplo.producto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.ejemplo.categoria.Categoria;
import org.ejemplo.categoria.RepositorioCategoria;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class ProductoCategoriaIntegracionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RepositorioProducto repositorioProducto;

    @Autowired
    private RepositorioCategoria repositorioCategoria;

    @BeforeEach
    void clean() {
        repositorioProducto.deleteAll();
        repositorioCategoria.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        repositorioProducto.deleteAll();
        repositorioCategoria.deleteAll();
    }

    @Test
    void productoSeCargaConSuCategoria() {
        Categoria bebidas = repositorioCategoria.save(new Categoria("Bebidas", "Sin alcohol"));
        Long productoId = repositorioProducto.save(new Producto("Jugo de naranja", null,
                new BigDecimal("2.50"), 10, null, bebidas)).getId();

        Producto encontrado = repositorioProducto.findById(productoId).orElseThrow();

        assertThat(encontrado.obtenerCategoria().getId()).isEqualTo(bebidas.getId());
        assertThat(encontrado.obtenerCategoria().obtenerNombre()).isEqualTo("Bebidas");
    }

    @Test
    void productoRequiereCategoria() {
        assertThatThrownBy(() -> repositorioProducto.saveAndFlush(new Producto("Sin categoria", null,
                new BigDecimal("1.00"), 1, null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void categoriaConProductosNoPuedeEliminarse() {
        Categoria bebidas = repositorioCategoria.save(new Categoria("Bebidas", null));
        repositorioProducto.save(new Producto("Agua", null, new BigDecimal("0.80"), 50, null, bebidas));

        assertThatThrownBy(() -> repositorioCategoria.deleteById(bebidas.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repositorioCategoria.existsById(bebidas.getId())).isTrue();
    }

    @Test
    void buscarPorCategoriaDevuelveSoloProductosDeEsaCategoria() {
        Categoria bebidas = repositorioCategoria.save(new Categoria("Bebidas", null));
        Categoria snacks = repositorioCategoria.save(new Categoria("Snacks", null));
        repositorioProducto.save(new Producto("Agua", null, new BigDecimal("0.80"), 50, null, bebidas));
        repositorioProducto.save(new Producto("Jugo", null, new BigDecimal("2.50"), 10, null, bebidas));
        repositorioProducto.save(new Producto("Papas", null, new BigDecimal("1.20"), 30, null, snacks));

        Page<Producto> page = repositorioProducto.buscarPorCategoriaId(bebidas.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Producto::obtenerNombre)
                .containsExactlyInAnyOrder("Agua", "Jugo");
    }

    @Test
    void buscarPorCategoriaYNombreCoincideSinDistinguirMayusculasDentroDeCategoria() {
        Categoria bebidas = repositorioCategoria.save(new Categoria("Bebidas", null));
        Categoria snacks = repositorioCategoria.save(new Categoria("Snacks", null));
        repositorioProducto.save(new Producto("Jugo de naranja", null, new BigDecimal("2.50"), 10, null, bebidas));
        repositorioProducto.save(new Producto("Jugo de manzana", null, new BigDecimal("2.30"), 5, null, snacks));

        Page<Producto> page = repositorioProducto.buscarPorCategoriaIdYNombreContieneIgnorandoMayusculas(
                bebidas.getId(), "JUGO", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).obtenerNombre()).isEqualTo("Jugo de naranja");
    }
}
