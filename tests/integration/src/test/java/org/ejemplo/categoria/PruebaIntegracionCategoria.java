package org.ejemplo.categoria;

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

@SpringBootTest(classes = org.example.CatalogApplication.class)
@AutoConfigureMockMvc
@Testcontainers
class CategoriaIntegrationTest {

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
    private RepositorioCategoria repositorioCategoria;

    @BeforeEach
    void clean() {
        repositorioCategoria.deleteAll();
    }

    @Test
    void crearLuegoConsultarLuegoListarDevuelveCategoria() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType("application/json")
                        .content("{\"nombre\":\"Bebidas\",\"descripcion\":\"Sin alcohol\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nombre").value("Bebidas"));

        Long id = repositorioCategoria.findAll().get(0).getId();

        mockMvc.perform(get("/api/categorias/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.descripcion").value("Sin alcohol"));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void actualizarCambiaCategoria() throws Exception {
        Long id = repositorioCategoria.save(new Categoria("Bebidas", null)).getId();

        mockMvc.perform(put("/api/categorias/{id}", id)
                        .contentType("application/json")
                        .content("{\"nombre\":\"Jugos\",\"descripcion\":\"Naturales\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jugos"))
                .andExpect(jsonPath("$.descripcion").value("Naturales"));
    }

    @Test
    void eliminarQuitaCategoria() throws Exception {
        Long id = repositorioCategoria.save(new Categoria("Bebidas", null)).getId();

        mockMvc.perform(delete("/api/categorias/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(repositorioCategoria.existsById(id)).isFalse();
    }

    @Test
    void crearConNombreVacioEsRechazado() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType("application/json")
                        .content("{\"nombre\":\"  \"}"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        assertThat(repositorioCategoria.count()).isZero();
    }
}