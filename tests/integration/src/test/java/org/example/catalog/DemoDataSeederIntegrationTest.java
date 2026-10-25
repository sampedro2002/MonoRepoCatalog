package org.example.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = org.example.CatalogApplication.class)
@ActiveProfiles("dev")
@Testcontainers
class DemoDataSeederIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CategoryRepository categories;

    @Autowired
    private ProductRepository products;

    @Autowired
    private DemoDataSeeder seeder;

    @Test
    void seedsCategoriesAndProductsOnEmptyDatabase() {
        assertThat(categories.count()).isEqualTo(3);
        assertThat(products.count()).isEqualTo(6);
    }

    @Test
    void runningSeederAgainDoesNotDuplicateData() {
        ApplicationArguments args = new DefaultApplicationArguments();

        seeder.run(args);
        seeder.run(args);

        assertThat(categories.count()).isEqualTo(3);
        assertThat(products.count()).isEqualTo(6);
    }
}
