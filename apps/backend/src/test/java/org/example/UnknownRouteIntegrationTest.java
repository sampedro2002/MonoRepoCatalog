package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Runs on a real embedded server, because unknown routes are only handled by the
 * {@code spring.mvc.throw-exception-if-no-handler-found} setting outside MockMvc.
 * Database auto-configuration is excluded so no PostgreSQL is needed.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        })
class UnknownRouteIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void unknownRouteReturnsProblemWithTraceId() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/unknown", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getFirst(GlobalExceptionHandler.TRACE_ID_HEADER)).isNotBlank();
        assertThat(response.getBody()).contains("\"traceId\"");
    }
}
