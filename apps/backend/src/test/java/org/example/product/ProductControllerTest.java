package org.example.product;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

/**
 * Web-layer tests for {@link ProductController}. Spring MVC runs for real (routing,
 * JSON serialization, Bean Validation and {@link GlobalExceptionHandler}); only the
 * service is replaced by a mock.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    /** Same value as GlobalExceptionHandler#TRACE_ID_HEADER, which is package-private. */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void listWithoutQueryReturnsTheWholeCatalog() throws Exception {
        given(productService.listProducts())
                .willReturn(List.of(product(1L, "Loja Coffee"), product(2L, "Ecuadorian Roses")));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Loja Coffee"))
                .andExpect(jsonPath("$[1].name").value("Ecuadorian Roses"));

        verify(productService).listProducts();
    }

    @Test
    void listWithQueryTrimsTheSearchTermAndDelegates() throws Exception {
        given(productService.searchByName("coffee")).willReturn(List.of(product(1L, "Loja Coffee")));

        mockMvc.perform(get("/api/products").param("q", "  coffee  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Loja Coffee"));

        verify(productService).searchByName("coffee");
        verify(productService, never()).listProducts();
    }

    @Test
    void blankQueryFallsBackToTheWholeCatalog() throws Exception {
        given(productService.listProducts()).willReturn(List.of(product(1L, "Loja Coffee")));

        mockMvc.perform(get("/api/products").param("q", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(productService).listProducts();
        verify(productService, never()).searchByName(any());
    }

    @Test
    void detailsReturnsTheProduct() throws Exception {
        given(productService.getProductById(7L)).willReturn(product(7L, "Pacific White Shrimp"));

        mockMvc.perform(get("/api/products/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Pacific White Shrimp"))
                .andExpect(jsonPath("$.price").value(3.50));
    }

    @Test
    void detailsReturnsRfc7807ProblemWithTraceIdWhenMissing() throws Exception {
        willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id 99"))
                .given(productService).getProductById(99L);

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(header().exists(TRACE_ID_HEADER))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createReturns201WithTheSavedProduct() throws Exception {
        given(productService.saveProduct(any(Product.class))).willReturn(product(15L, "Yellow Dragon Fruit"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Yellow Dragon Fruit",
                                  "description": "Sweet and refreshing",
                                  "price": 5.00,
                                  "stock": 120,
                                  "imageUrl": "https://example.com/dragon-fruit.png"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.name").value("Yellow Dragon Fruit"));

        verify(productService).saveProduct(any(Product.class));
    }

    @Test
    void createRejectsAnInvalidProductWith400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "price": -1, "stock": -5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists(TRACE_ID_HEADER));

        verify(productService, never()).saveProduct(any(Product.class));
    }

    @Test
    void updateReturnsTheModifiedProduct() throws Exception {
        given(productService.updateProduct(eq(3L), any(Product.class)))
                .willReturn(product(3L, "Nacional Fine Aroma Cocoa (organic)"));

        mockMvc.perform(put("/api/products/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Nacional Fine Aroma Cocoa (organic)", "price": 9.90, "stock": 200}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nacional Fine Aroma Cocoa (organic)"));

        verify(productService).updateProduct(eq(3L), any(Product.class));
    }

    @Test
    void deleteReturns204AndNeverTouchesTheBody() throws Exception {
        mockMvc.perform(delete("/api/products/4"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProductById(4L);
        verify(productService, never()).getProductById(anyLong());
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
