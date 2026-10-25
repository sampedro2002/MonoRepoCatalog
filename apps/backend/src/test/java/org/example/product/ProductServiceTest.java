package org.example.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Unit tests for {@link ProductService}. The repository is mocked, so these tests
 * need neither a database nor a Spring context.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void listProductsReturnsWhateverTheRepositoryFinds() {
        List<Product> stored = List.of(product(1L, "Loja Coffee"), product(2L, "Ecuadorian Roses"));
        when(productRepository.findAll()).thenReturn(stored);

        List<Product> result = productService.listProducts();

        assertThat(result).hasSize(2).extracting(Product::getName)
                .containsExactly("Loja Coffee", "Ecuadorian Roses");
        verify(productRepository).findAll();
    }

    @Test
    void searchByNameDelegatesToTheRepository() {
        List<Product> matches = List.of(product(3L, "Nacional Fine Aroma Cocoa"));
        when(productRepository.findByNameContainingIgnoreCase("cocoa")).thenReturn(matches);

        List<Product> result = productService.searchByName("cocoa");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Nacional Fine Aroma Cocoa");
        verify(productRepository).findByNameContainingIgnoreCase("cocoa");
    }

    @Test
    void getProductByIdReturnsTheStoredProduct() {
        Product stored = product(7L, "Pacari 70% Chocolate");
        when(productRepository.findById(7L)).thenReturn(Optional.of(stored));

        Product result = productService.getProductById(7L);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getName()).isEqualTo("Pacari 70% Chocolate");
    }

    @Test
    void getProductByIdThrowsNotFoundWhenTheProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND))
                .hasMessageContaining("99");
    }

    @Test
    void saveProductPersistsAndReturnsTheSavedEntity() {
        Product toSave = product(null, "Green Plantain Chips");
        Product saved = product(10L, "Green Plantain Chips");
        when(productRepository.save(toSave)).thenReturn(saved);

        Product result = productService.saveProduct(toSave);

        assertThat(result.getId()).isEqualTo(10L);
        verify(productRepository).save(toSave);
    }

    @Test
    void updateProductCopiesEveryEditableFieldAndSaves() {
        Product existing = product(5L, "Old name");
        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product changes = new Product();
        changes.setName("Updated name");
        changes.setDescription("Updated description");
        changes.setPrice(new BigDecimal("19.99"));
        changes.setStock(42);
        changes.setImageUrl("https://example.com/image.png");

        Product result = productService.updateProduct(5L, changes);

        assertThat(result.getId()).isEqualTo(5L); // id is never overwritten
        assertThat(result.getName()).isEqualTo("Updated name");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.getPrice()).isEqualByComparingTo("19.99");
        assertThat(result.getStock()).isEqualTo(42);
        assertThat(result.getImageUrl()).isEqualTo("https://example.com/image.png");
        verify(productRepository).save(existing);
    }

    @Test
    void updateProductThrowsNotFoundAndNeverSavesWhenTheProductDoesNotExist() {
        when(productRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(404L, product(null, "Ghost")))
                .isInstanceOf(ResponseStatusException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProductByIdDelegatesToTheRepository() {
        productService.deleteProductById(8L);

        verify(productRepository).deleteById(8L);
    }

    private static Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Description of " + name);
        product.setPrice(new BigDecimal("3.50"));
        product.setStock(10);
        product.setImageUrl("https://example.com/" + name.replace(' ', '-') + ".png");
        return product;
    }
}
