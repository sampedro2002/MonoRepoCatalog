package org.example.product;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.example.CatalogApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Full-stack tests for the product API: real Spring context, real HTTP server, real
 * PostgreSQL (Testcontainers) and the real Flyway migrations, including the seed.
 */
@SpringBootTest(classes = CatalogApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    /**
     * Production applies only the schema; QA and these tests also load the demo seed
     * (same setting as SPRING_FLYWAY_LOCATIONS in the Suitest workflow).
     */
    @DynamicPropertySource
    static void loadDemoSeed(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.locations", () -> "classpath:db/migration,classpath:db/seed");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void flywaySeedLeavesTheDemoCatalogAvailable() {
        ResponseEntity<Product[]> response = restTemplate.getForEntity("/api/products", Product[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(15);
        assertThat(response.getBody()).extracting(Product::getName).contains("Loja Coffee");
    }

    @Test
    void searchFiltersByNameIgnoringCase() {
        ResponseEntity<Product[]> response =
                restTemplate.getForEntity("/api/products?q=coffee", Product[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isNotEmpty();
        assertThat(response.getBody())
                .allSatisfy(p -> assertThat(p.getName().toLowerCase()).contains("coffee"));
    }

    @Test
    void createReadUpdateDeleteRoundTrip() {
        String uniqueName = "Integration product " + UUID.randomUUID();

        // CREATE
        ResponseEntity<Product> created = restTemplate.postForEntity(
                "/api/products", jsonEntity(uniqueName, "9.99", 25), Product.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        Long id = created.getBody().getId();
        assertThat(id).isNotNull();
        assertThat(created.getBody().getName()).isEqualTo(uniqueName);

        // READ
        ResponseEntity<Product> fetched =
                restTemplate.getForEntity("/api/products/{id}", Product.class, id);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody()).isNotNull();
        assertThat(fetched.getBody().getName()).isEqualTo(uniqueName);

        // UPDATE
        String updatedName = uniqueName + " (updated)";
        ResponseEntity<Product> updated = restTemplate.exchange(
                "/api/products/{id}",
                HttpMethod.PUT,
                jsonEntity(updatedName, "14.50", 3),
                Product.class,
                id);

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().getName()).isEqualTo(updatedName);
        assertThat(updated.getBody().getPrice()).isEqualByComparingTo("14.50");
        assertThat(updated.getBody().getStock()).isEqualTo(3);

        // DELETE
        ResponseEntity<Void> deleted =
                restTemplate.exchange("/api/products/{id}", HttpMethod.DELETE, null, Void.class, id);
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // GONE
        ResponseEntity<String> gone = restTemplate.getForEntity("/api/products/{id}", String.class, id);
        assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(gone.getBody()).contains("traceId");
        assertThat(gone.getHeaders().getFirst("X-Trace-Id")).isNotBlank();
    }

    @Test
    void invalidProductIsRejectedWithProblemDetail() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/products", jsonEntity("", "-1", -5), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("traceId");
        assertThat(response.getHeaders().getFirst("X-Trace-Id")).isNotBlank();
    }

    @Test
    void missingProductReturns404Problem() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/products/{id}", String.class, 999_999L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("traceId");
    }

    private static HttpEntity<String> jsonEntity(String name, String price, int stock) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {
                  "name": "%s",
                  "description": "Created by ProductApiIntegrationTest",
                  "price": %s,
                  "stock": %d,
                  "imageUrl": "https://example.com/test.png"
                }
                """.formatted(name, price, stock);
        return new HttpEntity<>(body, headers);
    }
}
