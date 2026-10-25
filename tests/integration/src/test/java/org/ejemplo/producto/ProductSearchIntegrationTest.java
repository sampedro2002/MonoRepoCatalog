package org.ejemplo.producto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.ejemplo.categoria.Categoria;
import org.ejemplo.categoria.RepositorioCategoria;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = org.example.CatalogApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class ProductSearchIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private RepositorioProducto repositorioProducto;

    @Autowired
    private RepositorioCategoria repositorioCategoria;

    @Autowired
    private MockMvc mockMvc;

    private Long drinksId;
    private Long snacksId;

    @BeforeEach
    void seed() {
        repositorioProducto.deleteAll();
        repositorioCategoria.deleteAll();

        drinksId = repositorioCategoria.save(new Categoria("Drinks", null)).getId();
        snacksId = repositorioCategoria.save(new Categoria("Snacks", null)).getId();

        productService.create("Orange Juice", null, new BigDecimal("2.50"), 10, null, drinksId);
        productService.create("Apple Juice", null, new BigDecimal("1.80"), 5, null, drinksId);
        productService.create("Water", null, new BigDecimal("0.80"), 50, null, drinksId);
        productService.create("Potato Chips", null, new BigDecimal("1.20"), 30, null, snacksId);
        productService.create("Juice Box", null, new BigDecimal("3.00"), 8, null, snacksId);
    }

    @AfterEach
    void cleanUp() {
        repositorioProducto.deleteAll();
        repositorioCategoria.deleteAll();
    }

    @Test
    void searchWithoutFiltersReturnsAllProductsPaged() {
        Page<Producto> page = productService.search(null, null, null, null, PageRequest.of(0, 2));

        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void searchFiltersByNameIgnoringCase() {
        Page<Producto> page = productService.search("JUICE", null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Producto::obtenerNombre)
                .containsExactlyInAnyOrder("Orange Juice", "Apple Juice", "Juice Box");
    }

    @Test
    void searchFiltersByCategory() {
        Page<Producto> page = productService.search(null, snacksId, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Producto::obtenerNombre)
                .containsExactlyInAnyOrder("Potato Chips", "Juice Box");
    }

    @Test
    void searchFiltersByPriceRangeInclusive() {
        Page<Producto> page = productService.search(null, null, new BigDecimal("1.80"),
                new BigDecimal("2.50"), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Producto::obtenerNombre)
                .containsExactlyInAnyOrder("Orange Juice", "Apple Juice");
    }

    @Test
    void searchCombinesAllFilters() {
        Page<Producto> page = productService.search("juice", drinksId, new BigDecimal("2.00"),
                null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Producto::obtenerNombre)
                .containsExactly("Orange Juice");
    }

    @Test
    void searchRejectsMinPriceGreaterThanMaxPrice() {
        assertThatThrownBy(() -> productService.search(null, null, new BigDecimal("5.00"),
                new BigDecimal("1.00"), PageRequest.of(0, 10)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void endpointReturnsFilteredPageWithMetadata() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("categoryId", drinksId.toString())
                        .param("size", "2")
                        .param("sort", "nombre,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Apple Juice"))
                .andExpect(jsonPath("$.content[0].categoryName").value("Drinks"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void endpointFiltersByNameAndPriceRange() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("name", "juice")
                        .param("minPrice", "2.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Orange Juice", "Juice Box")));
    }

    @Test
    void endpointReturnsBadRequestWhenMinPriceIsGreaterThanMaxPrice() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("minPrice", "5.00")
                        .param("maxPrice", "1.00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endpointReturnsEmptyPageWhenNothingMatches() throws Exception {
        mockMvc.perform(get("/api/products").param("name", "does-not-exist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
