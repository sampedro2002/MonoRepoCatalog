package org.example.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CategoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void clean() {
        categoryRepository.deleteAll();
    }

    @Test
    void createThenFetchThenListReturnsCategory() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content("{\"name\":\"Bebidas\",\"description\":\"Sin alcohol\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Bebidas"));

        Long id = categoryRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value("Sin alcohol"));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateChangesCategory() throws Exception {
        Long id = categoryRepository.save(new Category("Bebidas", null)).getId();

        mockMvc.perform(put("/api/categories/{id}", id)
                        .contentType("application/json")
                        .content("{\"name\":\"Jugos\",\"description\":\"Naturales\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jugos"))
                .andExpect(jsonPath("$.description").value("Naturales"));
    }

    @Test
    void deleteRemovesCategory() throws Exception {
        Long id = categoryRepository.save(new Category("Bebidas", null)).getId();

        mockMvc.perform(delete("/api/categories/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.existsById(id)).isFalse();
    }

    @Test
    void createWithBlankNameIsRejected() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType("application/json")
                        .content("{\"name\":\"  \"}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assertThat(categoryRepository.count()).isZero();
    }
}
