package org.ejemplo.producto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.ejemplo.categoria.Categoria;
import org.ejemplo.categoria.ExcepcionCategoriaNoEncontrada;
import org.ejemplo.categoria.ServicioCategoria;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private RepositorioProducto repositorioProducto;

    @Mock
    private ServicioCategoria servicioCategoria;

    @InjectMocks
    private ProductService productService;

    private final Categoria drinks = new Categoria("Drinks", null);

    @Test
    void searchDelegatesToRepositoryWithPageable() {
        Pageable pageable = PageRequest.of(1, 5);
        Page<Producto> expected = new PageImpl<>(List.of(new Producto("Juice", null,
                new BigDecimal("2.50"), 10, null, drinks)));
        when(repositorioProducto.findAll(any(Specification.class), any(Pageable.class))).thenReturn(expected);

        Page<Producto> result = productService.search("juice", 1L, new BigDecimal("1.00"),
                new BigDecimal("3.00"), pageable);

        assertThat(result).isSameAs(expected);
        verify(repositorioProducto).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void searchAcceptsAllFiltersEmpty() {
        when(repositorioProducto.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<Producto> result = productService.search(null, null, null, null, PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void searchRejectsMinPriceGreaterThanMaxPrice() {
        assertThatThrownBy(() -> productService.search(null, null, new BigDecimal("5.00"),
                new BigDecimal("1.00"), PageRequest.of(0, 20)))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("minPrice cannot be greater than maxPrice");
        verify(repositorioProducto, never()).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void createSavesProductWhenDataIsValid() {
        when(servicioCategoria.buscarPorId(1L)).thenReturn(drinks);
        when(repositorioProducto.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto created = productService.create("Juice", "Orange", new BigDecimal("2.50"), 10,
                "https://example.com/juice.png", 1L);

        assertThat(created.obtenerNombre()).isEqualTo("Juice");
        assertThat(created.obtenerDescripcion()).isEqualTo("Orange");
        assertThat(created.obtenerPrecio()).isEqualByComparingTo("2.50");
        assertThat(created.obtenerExistencias()).isEqualTo(10);
        assertThat(created.obtenerUrlImagen()).isEqualTo("https://example.com/juice.png");
        assertThat(created.obtenerCategoria()).isSameAs(drinks);
        verify(repositorioProducto).save(created);
    }

    @Test
    void createAcceptsZeroPriceAndStock() {
        when(servicioCategoria.buscarPorId(1L)).thenReturn(drinks);
        when(repositorioProducto.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Producto created = productService.create("Free sample", null, BigDecimal.ZERO, 0, null, 1L);

        assertThat(created.obtenerPrecio()).isZero();
        assertThat(created.obtenerExistencias()).isZero();
    }

    @Test
    void createFailsWhenCategoryDoesNotExist() {
        when(servicioCategoria.buscarPorId(99L)).thenThrow(new ExcepcionCategoriaNoEncontrada(99L));

        assertThatThrownBy(() -> productService.create("Juice", null, new BigDecimal("2.50"), 10, null, 99L))
                .isInstanceOf(ExcepcionCategoriaNoEncontrada.class);
        verify(repositorioProducto, never()).save(any());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "   " })
    void createRejectsBlankName(String name) {
        assertThatThrownBy(() -> productService.create(name, null, new BigDecimal("1.00"), 1, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name is required");
        verify(repositorioProducto, never()).save(any());
    }

    @Test
    void createRejectsNameLongerThan150Characters() {
        String name = "a".repeat(151);

        assertThatThrownBy(() -> productService.create(name, null, new BigDecimal("1.00"), 1, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name cannot exceed 150 characters");
    }

    @Test
    void createRejectsDescriptionLongerThan500Characters() {
        String description = "a".repeat(501);

        assertThatThrownBy(() -> productService.create("Juice", description, new BigDecimal("1.00"), 1, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description cannot exceed 500 characters");
    }

    @Test
    void createRejectsMissingPrice() {
        assertThatThrownBy(() -> productService.create("Juice", null, null, 1, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price is required");
    }

    @Test
    void createRejectsNegativePrice() {
        assertThatThrownBy(() -> productService.create("Juice", null, new BigDecimal("-0.01"), 1, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price cannot be negative");
    }

    @Test
    void createRejectsPriceWithMoreThanTwoDecimals() {
        assertThatThrownBy(() -> productService.create("Juice", null, new BigDecimal("2.505"), 1, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price must have up to 10 integer digits and 2 decimals");
    }

    @Test
    void createRejectsPriceWithMoreThanTenIntegerDigits() {
        assertThatThrownBy(() -> productService.create("Juice", null, new BigDecimal("12345678901"), 1, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price must have up to 10 integer digits and 2 decimals");
    }

    @Test
    void createRejectsMissingStock() {
        assertThatThrownBy(() -> productService.create("Juice", null, new BigDecimal("2.50"), null, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stock is required");
    }

    @Test
    void createRejectsNegativeStock() {
        assertThatThrownBy(() -> productService.create("Juice", null, new BigDecimal("2.50"), -1, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Stock cannot be negative");
    }

    @Test
    void createRejectsImageUrlLongerThan500Characters() {
        String imageUrl = "a".repeat(501);

        assertThatThrownBy(() -> productService.create("Juice", null, new BigDecimal("2.50"), 1, imageUrl, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image URL cannot exceed 500 characters");
    }

    @Test
    void updateChangesFieldsAndCategory() {
        Producto existing = new Producto("Juice", null, new BigDecimal("2.50"), 10, null, drinks);
        Categoria snacks = new Categoria("Snacks", null);
        when(repositorioProducto.findById(1L)).thenReturn(Optional.of(existing));
        when(servicioCategoria.buscarPorId(2L)).thenReturn(snacks);

        Producto updated = productService.update(1L, "Chips", "Salted", new BigDecimal("1.20"), 30,
                null, 2L);

        assertThat(updated).isSameAs(existing);
        assertThat(updated.obtenerNombre()).isEqualTo("Chips");
        assertThat(updated.obtenerDescripcion()).isEqualTo("Salted");
        assertThat(updated.obtenerPrecio()).isEqualByComparingTo("1.20");
        assertThat(updated.obtenerExistencias()).isEqualTo(30);
        assertThat(updated.obtenerCategoria()).isSameAs(snacks);
    }

    @Test
    void updateFailsWhenProductDoesNotExist() {
        when(repositorioProducto.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, "Juice", null, new BigDecimal("2.50"), 10,
                null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product 99 not found");
        verify(servicioCategoria, never()).buscarPorId(anyLong());
    }

    @Test
    void updateValidatesBeforeLookingUpProduct() {
        assertThatThrownBy(() -> productService.update(1L, " ", null, new BigDecimal("2.50"), 10, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product name is required");
        verify(repositorioProducto, never()).findById(anyLong());
    }
}
