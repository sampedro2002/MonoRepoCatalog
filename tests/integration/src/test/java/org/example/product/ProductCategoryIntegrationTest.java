package org.example.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.example.category.Category;
import org.example.category.CategoryRepository;
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
class ProductCategoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void clean() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void productIsLoadedWithItsCategory() {
        Category bebidas = categoryRepository.save(new Category("Bebidas", "Sin alcohol"));
        Long productId = productRepository.save(new Product("Jugo de naranja", null,
                new BigDecimal("2.50"), 10, null, bebidas)).getId();

        Product found = productRepository.findById(productId).orElseThrow();

        assertThat(found.getCategory().getId()).isEqualTo(bebidas.getId());
        assertThat(found.getCategory().getName()).isEqualTo("Bebidas");
    }

    @Test
    void productRequiresCategory() {
        assertThatThrownBy(() -> productRepository.saveAndFlush(new Product("Sin categoria", null,
                new BigDecimal("1.00"), 1, null, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void categoryWithProductsCannotBeDeleted() {
        Category bebidas = categoryRepository.save(new Category("Bebidas", null));
        productRepository.save(new Product("Agua", null, new BigDecimal("0.80"), 50, null, bebidas));

        assertThatThrownBy(() -> categoryRepository.deleteById(bebidas.getId()))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThat(categoryRepository.existsById(bebidas.getId())).isTrue();
    }

    @Test
    void findByCategoryReturnsOnlyProductsOfThatCategory() {
        Category bebidas = categoryRepository.save(new Category("Bebidas", null));
        Category snacks = categoryRepository.save(new Category("Snacks", null));
        productRepository.save(new Product("Agua", null, new BigDecimal("0.80"), 50, null, bebidas));
        productRepository.save(new Product("Jugo", null, new BigDecimal("2.50"), 10, null, bebidas));
        productRepository.save(new Product("Papas", null, new BigDecimal("1.20"), 30, null, snacks));

        Page<Product> page = productRepository.findByCategoryId(bebidas.getId(), PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("Agua", "Jugo");
    }

    @Test
    void findByCategoryAndNameMatchesCaseInsensitivelyWithinCategory() {
        Category bebidas = categoryRepository.save(new Category("Bebidas", null));
        Category snacks = categoryRepository.save(new Category("Snacks", null));
        productRepository.save(new Product("Jugo de naranja", null, new BigDecimal("2.50"), 10, null, bebidas));
        productRepository.save(new Product("Jugo de manzana", null, new BigDecimal("2.30"), 5, null, snacks));

        Page<Product> page = productRepository.findByCategoryIdAndNameContainingIgnoreCase(
                bebidas.getId(), "JUGO", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("Jugo de naranja");
    }
}
